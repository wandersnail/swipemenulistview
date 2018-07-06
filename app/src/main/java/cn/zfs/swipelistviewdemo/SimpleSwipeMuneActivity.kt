/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 baoyongzhang <baoyz94@gmail.com>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package cn.zfs.swipelistviewdemo

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import cn.zfs.swipemenulistview.SwipeController
import cn.zfs.swipemenulistview.SwipeMenuCreator
import cn.zfs.swipemenulistview.SwipeMenuItem
import cn.zfs.swipemenulistview.SwipeMenuListView

/**
 * SwipeMenuListView
 * Created by baoyz on 15/6/29.  */
class SimpleSwipeMuneActivity : AppCompatActivity() {

    private var mAppList: MutableList<ApplicationInfo>? = null
    private var mAdapter: AppAdapter? = null
    private var mListView: SwipeMenuListView? = null
    private var toast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mListView = SwipeMenuListView(this)
        setContentView(mListView, ViewGroup.LayoutParams(-1, -1))
        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT)
        mAppList = packageManager.getInstalledApplications(0)

        mAdapter = AppAdapter()
        mListView!!.adapter = mAdapter

        // step 1. create a MenuCreator
        val creator = SwipeMenuCreator { menu ->
            // create "open" item
            val openItem = SwipeMenuItem(
                    applicationContext)
            // set item background
            openItem.background = ColorDrawable(Color.rgb(0xC9, 0xC9,
                    0xCE))
            // set item width
            openItem.width = dp2px(90)
            // set item title
            openItem.title = "Open"
            // set item title fontsize
            openItem.titleSize = dp2px(18)
            // set item title font color
            openItem.titleColor = Color.WHITE
            // add to menu
            menu.addMenuItem(openItem)

            // create "delete" item
            val deleteItem = SwipeMenuItem(
                    applicationContext)
            // set item background
            deleteItem.background = ColorDrawable(Color.rgb(0xF9,
                    0x3F, 0x25))
            // set item width
            deleteItem.width = dp2px(90)
            // set a icon
            deleteItem.setIcon(R.mipmap.ic_delete)
            // add to menu
            menu.addMenuItem(deleteItem)
        }
        // set creator
        mListView!!.setMenuCreator(creator)

        // step 2. listener item click event
        mListView!!.setOnMenuItemClickListener { position, menu, index ->
            val item = mAppList!![position]
            when (index) {
                0 ->
                    // open
                    open(item)
                1 -> {
                    // delete
                    //					delete(item);
                    mAppList!!.removeAt(position)
                    mAdapter!!.notifyDataSetChanged()
                }
            }
            false
        }

        // set SwipeListener
        mListView!!.setOnSwipeListener(object : SwipeMenuListView.OnSwipeListener {

            override fun onSwipeStart(position: Int) {
                // swipe start
            }

            override fun onSwipeEnd(position: Int) {
                // swipe end
            }
        })

        // set MenuStateChangeListener
        mListView!!.setOnMenuStateChangeListener(object : SwipeMenuListView.OnMenuStateChangeListener {
            override fun onMenuOpen(position: Int) {}

            override fun onMenuClose(position: Int) {}
        })

        // other setting
        //		listView.setCloseInterpolator(new BounceInterpolator());

        // test item long click
        mListView!!.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, view, position, id ->
            showSingleToast(position.toString() + " long click")
            false
        }

        mListView!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id -> showSingleToast(position.toString() + "click") }

    }

    private fun showSingleToast(s: String) {
        toast!!.setText(s)
        toast!!.show()
    }

    private fun delete(item: ApplicationInfo) {
        // delete app
        try {
            val intent = Intent(Intent.ACTION_DELETE)
            intent.data = Uri.fromParts("package", item.packageName, null)
            startActivity(intent)
        } catch (e: Exception) {
        }

    }

    private fun open(item: ApplicationInfo) {
        // open app
        val resolveIntent = Intent(Intent.ACTION_MAIN, null)
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        resolveIntent.setPackage(item.packageName)
        val resolveInfoList = packageManager
                .queryIntentActivities(resolveIntent, 0)
        if (resolveInfoList != null && resolveInfoList.size > 0) {
            val resolveInfo = resolveInfoList[0]
            val activityPackageName = resolveInfo.activityInfo.packageName
            val className = resolveInfo.activityInfo.name

            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            val componentName = ComponentName(
                    activityPackageName, className)

            intent.component = componentName
            startActivity(intent)
        }
    }

    internal inner class AppAdapter : BaseAdapter(), SwipeController {

        override fun getCount(): Int {
            return mAppList!!.size
        }

        override fun getItem(position: Int): ApplicationInfo {
            return mAppList!![position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var view = convertView
            if (view == null) {
                view = View.inflate(applicationContext,
                        R.layout.item_swipe_list_app, null)
                ViewHolder(view!!)
            }
            val holder = view.tag as ViewHolder
            val item = getItem(position)
            holder.iv_icon.setImageDrawable(item.loadIcon(packageManager))
            holder.tv_name.text = item.loadLabel(packageManager)
            return view
        }

        internal inner class ViewHolder(view: View) {
            var iv_icon: ImageView
            var tv_name: TextView

            init {
                iv_icon = view.findViewById<View>(R.id.iv_icon) as ImageView
                tv_name = view.findViewById<View>(R.id.tv_name) as TextView
                view.tag = this
            }
        }

        override fun getSwipeEnableByPosition(position: Int): Boolean {
            return true
        }
    }

    private fun dp2px(dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),
                resources.displayMetrics).toInt()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_simple_swipe, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_left) {
            mListView!!.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT)
            return true
        }
        if (id == R.id.action_right) {
            mListView!!.setSwipeDirection(SwipeMenuListView.DIRECTION_RIGHT)
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}
