package com.wqh.app // 替换为您的包名

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView

class BlacklistAdapter(
    context: Context,
    private val appList: List<AppInfo>
) : ArrayAdapter<AppInfo>(context, 0, appList) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = inflater.inflate(R.layout.list_item_app, parent, false)
            holder = ViewHolder()
            holder.icon = view.findViewById(R.id.image_app_icon_blacklist)
            holder.name = view.findViewById(R.id.text_app_name_blacklist)
            holder.checkbox = view.findViewById(R.id.checkbox_app_blacklist)
            view.tag = holder
        } else {
            view = convertView
            holder = convertView.tag as ViewHolder
        }

        val appInfo = getItem(position)
        if (appInfo != null) {
            holder.icon.setImageDrawable(appInfo.icon)
            holder.name.text = appInfo.appName
            holder.checkbox.isChecked = appInfo.isBlacklisted
        }
        return view
    }

    private class ViewHolder {
        lateinit var icon: ImageView
        lateinit var name: TextView
        lateinit var checkbox: CheckBox
    }

    // 如果需要从外部更新单个 item 的状态，可以添加此方法
    // fun updateItem(appInfo: AppInfo, isBlacklisted: Boolean) {
    //     val index = appList.indexOf(appInfo)
    //     if (index != -1) {
    //         appList[index].isBlacklisted = isBlacklisted
    //         // notifyDataSetChanged() // 或者只更新单个item view
    //     }
    // }
}