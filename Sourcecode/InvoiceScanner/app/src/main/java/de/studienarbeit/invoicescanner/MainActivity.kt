package de.studienarbeit.invoicescanner


import android.content.res.Configuration
import android.graphics.Point
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import java.io.File


class MainActivity : AppCompatActivity(), RetakeConfirmFragment.onButtonClickedListener, CameraFragment.onImageTakenListener {

    val camera_fragment : CameraFragment = CameraFragment.newInstance()

    override fun onImageTaken(file : File) {

        val fragment = RetakeConfirmFragment()
        val args = Bundle()
        args.putString("imagepath",file.absolutePath)
        fragment.arguments = args
        supportFragmentManager.beginTransaction().replace(R.id.container,fragment).commit()
    }


    override fun onButtonAnalyze() {
        val text = "Image is now getting analyzed lulz"
        val duration = Toast.LENGTH_SHORT

        val toast = Toast.makeText(applicationContext, text, duration)
        toast.show()
    }

    override fun onButtonDismiss() {
        supportFragmentManager.beginTransaction().replace(R.id.container,camera_fragment).commit()
    }

    private var mDrawerLayout : DrawerLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        setContentView(R.layout.activity_main)
        savedInstanceState ?: supportFragmentManager.beginTransaction()
                .add(R.id.container, camera_fragment)
                .commit()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayShowTitleEnabled(false)
        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_white)


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
                    {supportFragmentManager.beginTransaction().replace(R.id.container, camera_fragment).commit()
                    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
                    actionbar.setDisplayShowTitleEnabled(false)
                    toolbar.setBackgroundResource(R.color.transparent)}

                R.id.nav_archive ->
                    {supportFragmentManager.beginTransaction().replace(R.id.container, ArchiveFragment()).commit()
                    window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
                    actionbar.setTitle(R.string.archive)
                    actionbar.setDisplayShowTitleEnabled(true)
                    toolbar.setBackgroundResource(R.color.colorPrimaryDark)}

                R.id.nav_favorites ->
                    {supportFragmentManager.beginTransaction().replace(R.id.container, FavoritesFragment()).commit()
                    window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
                    actionbar.setTitle(R.string.favorites)
                    actionbar.setDisplayShowTitleEnabled(true)
                    toolbar.setBackgroundResource(R.color.colorPrimaryDark)}

                R.id.nav_about ->
                    {supportFragmentManager.beginTransaction().replace(R.id.container, AboutFragment()).commit()
                    window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
                    actionbar.setTitle(R.string.about)
                    actionbar.setDisplayShowTitleEnabled(true)
                    toolbar.setBackgroundResource(R.color.colorPrimaryDark)}
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
            camera_fragment.windowResolution = Point(resolution.y,resolution.x)
        }
        else
        {
            camera_fragment.windowResolution = resolution
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                mDrawerLayout!!.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


}
