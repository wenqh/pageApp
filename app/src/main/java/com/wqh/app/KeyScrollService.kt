package com.wqh.app // 替换为您的包名

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class KeyScrollService : AccessibilityService() {

    private val TAG = "KeyScrollService"
    private var currentAppPackageName: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "无障碍服务已连接")
        // 服务连接后，可以在这里设置 AccessibilityServiceInfo，但我们已通过 XML 配置
        // val serviceInfo = AccessibilityServiceInfo()
        // serviceInfo.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        // serviceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        // serviceInfo.flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        // serviceInfo.packageNames = null // 监听所有应用
        // setServiceInfo(serviceInfo)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 获取当前活动窗口的应用包名，用于黑名单检查
        event?.let {
            if (it.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                it.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                if (it.packageName != null && it.packageName.isNotEmpty()) {
                    currentAppPackageName = it.packageName.toString()
                    // Log.d(TAG, "Current App: $currentAppPackageName")
                }
            }
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        // 只处理按键按下的事件
        if (event.action != KeyEvent.ACTION_DOWN) {
            return super.onKeyEvent(event)
        }
        var scrollAction: Int? = null
        when (event.keyCode) {
            KeyEvent.KEYCODE_F2 -> {
                scrollAction = AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD // 向上滚动或向前翻页
                Log.d(TAG, "音量上键按下 - 尝试向上滚动")
            }
            KeyEvent.KEYCODE_F1/*, KeyEvent.KEYCODE_VOLUME_UP*/ -> {
                scrollAction = AccessibilityNodeInfo.ACTION_SCROLL_FORWARD // 向下滚动或向后翻页
                Log.d(TAG, "音量下键按下 - 尝试向下滚动")
            }
        }

        if (scrollAction == null) {
            return super.onKeyEvent(event) //其他按键事件或ACTION_UP事件，交给系统处理
        }
        // 检查当前应用是否在黑名单中
        if (currentAppPackageName != null &&
            BlacklistPersistence.isAppBlacklisted(this, currentAppPackageName!!)) {
            Log.i(TAG, "$currentAppPackageName 在黑名单中，忽略滚动操作。")
            return super.onKeyEvent(event) // 如果在黑名单中，不处理事件，传递给系统
        }
        findScrollableNodeRecursive(rootInActiveWindow, scrollAction)?.run {
            Log.i(TAG, "在节点上执行滚动操作: ${this.className}")
            performAction(scrollAction)
        }

        return true // 返回true表示事件已被处理，不会传递给系统（即不会调整音量）
    }

    /**
     * 递归查找可执行指定滚动操作的可见节点。
     * 优先选择那些直接支持所需滚动方向的节点。
     */
    private fun findScrollableNodeRecursive(node: AccessibilityNodeInfo?, action: Int): AccessibilityNodeInfo? {
        if (node == null) return null

        // 检查当前节点是否可滚动且支持指定操作
        if (node.isScrollable && /*node.isVisibleToUser &&*/
//            node.className != "android.widget.HorizontalScrollView" && //下面action已经判断了
//            node.className != "androidx.viewpager2.widget.ViewPager2" &&
//            node.className != "androidx.viewpager.widget.ViewPager" &&
//            node.className != "android.support.v4.view.ViewPager" &&
            node.actionList.any { it.id == action/*AccessibilityNodeInfo.AccessibilityAction.ACTION_PAGE_UP.id
                        || it.id == AccessibilityNodeInfo.AccessibilityAction.ACTION_PAGE_DOWN.id
                        || it.id == AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_UP.id
                        || it.id == AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_DOWN.id
                        || it.id == AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD.id
                        || it.id == AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD.id*/
            } && node.actionList.none({it.id == AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_RIGHT.id || it.id == AccessibilityNodeInfo.AccessibilityAction.ACTION_PAGE_RIGHT.id})) {
            return node // 直接返回当前节点
        }

        // 递归查找子节点
        // 为了优化，可以优先查找特定类型的容器，如 RecyclerView, ListView, ScrollView, ViewPager2等
        // 但一个通用的方法是遍历所有子节点
        for (i in 0 until node.childCount) {
            val find =  findScrollableNodeRecursive(node.getChild(i), action)
            if (find != null) {
                return find
            }
        }
        return null // 未找到
    }

    //导刊，避开左右滚动
    private fun findScrollableNodeRecursive2(node: AccessibilityNodeInfo?, actionToPerform: Int): AccessibilityNodeInfo? {
        if (node == null) return null

        // 检查当前节点是否是可滚动视图并支持特定的垂直操作
        if (node.isScrollable && node.isVisibleToUser) {
            val supportsVerticalScroll = node.actionList.any {
                it.id == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD ||
                        it.id == AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD ||
                        // 考虑更具体的垂直滚动动作（API 23+）
                        it.id == AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_UP.id || // 注意这里是 .id
                        it.id == AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_DOWN.id
            }

            // 检查是否有水平滚动操作，以排除 Tab 布局
            val supportsHorizontalScroll = node.actionList.any {
                // 针对 API 23+ 的水平滚动动作
                it.id == AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_LEFT.id || // 注意这里是 .id
                        it.id == AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_RIGHT.id
            }

            if (supportsVerticalScroll && !supportsHorizontalScroll) {
                // 这个节点看起来主要是垂直可滚动的
                Log.d(TAG, "找到垂直可滚动节点: ${node.className}, viewId: ${node.viewIdResourceName}")
                return node
            } else if (supportsVerticalScroll && supportsHorizontalScroll) {
                // 这个节点同时支持两种。为了避免误触 Tab，优先检查子节点。
                // 此时，不立即返回当前节点，而是继续向下递归。
                Log.d(TAG, "节点同时支持垂直和水平滚动，继续查找子节点: ${node.className}")
            }
        }

        // 递归搜索子节点
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            val foundInChild = findScrollableNodeRecursive2(child, actionToPerform)
            if (foundInChild != null) {
                child?.recycle() // 回收当前子节点，因为找到了更深层的目标
                return foundInChild
            }
            child?.recycle() // 如果在子树中没有找到可滚动节点，回收子节点
        }

        // 如果没有子节点更合适，并且当前节点支持垂直滚动（即使它也支持水平滚动），则返回它。
        // 这样做是为了在没有更明确的垂直滚动节点时，不至于完全找不到节点。
        // 但是，如果你希望更严格地只滚动纯垂直的视图，可以考虑移除这部分或调整逻辑。
        if (node.isScrollable && node.isVisibleToUser) {
            val supportsVerticalScroll = node.actionList.any {
                it.id == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD ||
                        it.id == AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD ||
                        it.id == AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_UP.id ||
                        it.id == AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_DOWN.id
            }
            val supportsHorizontalScroll = node.actionList.any {
                it.id == AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_LEFT.id ||
                        it.id == AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_RIGHT.id
            }

            // 如果没有找到纯垂直的，但当前节点支持垂直滚动，就接受它
            if (supportsVerticalScroll) {
                Log.d(TAG, "在子节点搜索后返回一个通用垂直可滚动节点（可能也支持水平）: ${node.className}, viewId: ${node.viewIdResourceName}")
                return node
            }
        }

        return null
    }


    override fun onInterrupt() {
        Log.w(TAG, "无障碍服务被中断")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.w(TAG, "无障碍服务已销毁")
    }
}