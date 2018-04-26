package de.studienarbeit.invoicescanner


import android.content.res.Configuration
import android.graphics.Point
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import java.io.File
import android.arch.persistence.room.Room
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import de.studienarbeit.invoicescanner.fragments.*
import de.studienarbeit.invoicescanner.fragments.RecyclerViewFragment


class MainActivity : AppCompatActivity(), RetakeConfirmFragment.OnButtonClickedListener, CameraFragment.onImageTakenListener {
    override fun onImageSaved(path: String) {
        val imageAnalyer = ImageAnalyzer(this, path)
        imageAnalyer.analyse()
        currentInvoice = imageAnalyer.getInvoice()
    }

    private val cameraFragment : CameraFragment = CameraFragment.newInstance()
    private val archiveFragment = ArchiveFragment()
    private val favoritesFragment = FavoritesFragment()
    private val aboutFragment = AboutFragment()
    private val recyclerViewFragment = RecyclerViewFragment()
    private val retakeConfirmFragment = RetakeConfirmFragment()
    private val pictureAnalyzedFragment = PictureAnalyzedFragment()
    private var currentFragment : android.support.v4.app.Fragment? = null
    private var currentImagePath : String = ""

    private lateinit var toolbar : Toolbar
    private var actionbar : ActionBar? = null

    private var hideIcon = true
    private var isMenuAvailable = true
    lateinit var currentInvoice : Invoice

    lateinit var db : AppDatabase


    override fun onImageTaken(file : File) {
        currentImagePath = file.absolutePath
        val args = Bundle()
        args.putString("imagepath",currentImagePath)
        retakeConfirmFragment.arguments = args
        setFragment(retakeConfirmFragment)
    }

    override fun onButtonAnalyze() {
        val args = Bundle()
        args.putString("imagepath", currentImagePath)
        pictureAnalyzedFragment.arguments = args
        setFragment(pictureAnalyzedFragment)
        hideIcon = false
        invalidateOptionsMenu()
    }

    override fun onButtonDismiss() {
        supportFragmentManager.popBackStack()
        actionbar!!.setHomeAsUpIndicator(R.drawable.ic_menu_white)
        currentFragment = cameraFragment
    }

    private var mDrawerLayout : DrawerLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        //window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        //window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        setContentView(R.layout.activity_main)
        savedInstanceState ?: supportFragmentManager.beginTransaction()
                .add(R.id.container, cameraFragment)
                .commit()

        db = Room.databaseBuilder(applicationContext,
                AppDatabase::class.java, "database-name").build()

        object : AsyncTask<Void, Void, Int>() {
            override fun doInBackground(vararg params: Void): Int? {
                recyclerViewFragment.initDataset(db.invoiceDao().all)
                return 0
            }

            override fun onPostExecute(resultCode: Int?) {
            }
        }.execute()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        actionbar = supportActionBar
        actionbar!!.setDisplayShowTitleEnabled(false)
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar!!.setHomeAsUpIndicator(R.drawable.ic_menu_white)
        currentFragment = cameraFragment

        mDrawerLayout = findViewById(R.id.drawer_layout)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            // set item as selected to persist highlight
            menuItem.isChecked = true
            // close drawer when item is tapped
            mDrawerLayout!!.closeDrawers()

            // Add code here to update the UI based on the item selected
            // For example, swap UI fragments here
            when(menuItem.itemId){
                R.id.nav_camera ->
                    {setFragment(cameraFragment)}

                R.id.nav_archive ->
                    {setFragment(recyclerViewFragment)}

                R.id.nav_favorites ->
                    {setFragment(favoritesFragment)}

                R.id.nav_about ->
                    {setFragment(aboutFragment)}
            }

            true
        }


        // MACHE WOANDERS
        val display = windowManager.defaultDisplay
        val resolution = Point()
        display.getRealSize(resolution)

        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            cameraFragment.windowResolution = Point(resolution.y,resolution.x)
        }
        else
        {
            cameraFragment.windowResolution = resolution
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_save, menu)
        menu!!.findItem(R.id.save_button).isVisible = !hideIcon
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if(isMenuAvailable){
                    mDrawerLayout!!.openDrawer(GravityCompat.START)
                } else {
                    supportFragmentManager.popBackStack()
                    if(currentFragment == pictureAnalyzedFragment) {
                        setFullscreenMode(true)
                        hideIcon = true
                        invalidateOptionsMenu()
                        currentFragment = retakeConfirmFragment
                    } else if (currentFragment == retakeConfirmFragment) {
                        actionbar!!.setHomeAsUpIndicator(R.drawable.ic_menu_white)
                        currentFragment = cameraFragment
                        isMenuAvailable = true
                    }
                }
                return true
            }
            R.id.save_button -> {
                onSaveButtonClicked()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onSaveButtonClicked()
    {
        object : AsyncTask<Void, Void, Int>() {
            override fun doInBackground(vararg params: Void): Int? {
                db.invoiceDao().insertInvoice(currentInvoice)
                recyclerViewFragment.initDataset(db.invoiceDao().all)
                return 0
            }

            override fun onPostExecute(resultCode: Int?) {
            }
        }.execute()

        hideIcon = true
        invalidateOptionsMenu()
        var bmp = BitmapFactory.decodeFile(currentInvoice.imagePath)
        var timestamp  = (System.currentTimeMillis()/1000).toString()
        var description = "test"
        var uri = MediaStore.Images.Media.insertImage(contentResolver, bmp, timestamp , description);
        Toast.makeText(applicationContext, uri, Toast.LENGTH_LONG).show()
        setFragment(recyclerViewFragment)
        setFullscreenMode(false)
        actionbar!!.setTitle(R.string.archive)
    }

    private fun setFullscreenMode(yes : Boolean) {
        if(yes) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
            actionbar!!.setDisplayShowTitleEnabled(false)
            toolbar.setBackgroundResource(R.color.transparent)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
            actionbar!!.setDisplayShowTitleEnabled(true)
            toolbar.setBackgroundResource(R.color.colorPrimaryDark)
        }
    }

    private fun setFragment(fragment : Fragment) {
        if(currentFragment != fragment) {
            runOnUiThread {
                if (currentFragment == cameraFragment || currentFragment == retakeConfirmFragment) {
                    supportFragmentManager.beginTransaction().replace(R.id.container, fragment).addToBackStack(null).commit()
                } else {
                    supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
                }
                currentFragment = fragment
                fragment as FragmentAttributeInterface
                setFullscreenMode(fragment.fullScreen)
                actionbar!!.title = fragment.actionBarTitle
                isMenuAvailable = fragment.isMenuAvailable
                if(isMenuAvailable) {
                    actionbar!!.setHomeAsUpIndicator(R.drawable.ic_menu_white)
                } else {
                    actionbar!!.setHomeAsUpIndicator(R.drawable.ic_menu_back)
                }
            }
        }
    }
}
