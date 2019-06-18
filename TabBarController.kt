import java.util.ArrayList
import java.util.Stack
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.octopepper.yummypets.common.adapter.YPWeakReference

interface TabBarViewListener {
    fun tabBarViewDidTapTab(tabIndex: Int)
}


class TabBarController(fragments: List<Fragment>,
                       private val fragmentManager: FragmentManager,
                       private val mContainerId: Int) : TabBarViewListener {

    interface Delegate {
        fun tabBarControlleShouldShowTabAtIndex(tabIndex: Int): Boolean
        fun tabBarControlleDidGoBackToTabAtIndex(tabIndex: Int)
        fun tabBarControllerDidShowFragment(fragment: Fragment)
        fun tabBarClickRootFragmentAgain()
    }

    object UniqueTagGenerator {
        var counter: Int = 0

        fun generateUniqueTag(fragment: Fragment): String {
            counter++
            return fragment.javaClass.name + counter
        }
    }

    class TabHistory : Stack<Int>() {

        // We only want a single tab instance in the history
        // at any given time so here we make sure to remove
        // any existing one before pushing.
        override fun push(item: Int?): Int {
            remove(item)
            return super.push(item)
        }
    }

    var delegate: Delegate? by YPWeakReference()
    private val tabHistory = TabHistory()
    private val fragmentStacks: MutableList<Stack<Fragment>>
    private var currentStackIndex: Int = 0

    fun isShowingARootFragment() : Boolean {
        return fragmentStacks[currentStackIndex].size == 1
    }

    init {
        UniqueTagGenerator.counter = 0
        val numberOfTabs = fragments.count()
        fragmentStacks = ArrayList(numberOfTabs)

        for (i in 0 until numberOfTabs) {
            val stack = Stack<Fragment>()
            val fragment = fragments[i]
            stack.add(fragment)
            fragmentStacks.add(stack)
        }

        // Setup fist fragment
        val fragment = fragmentStacks[currentStackIndex].peek()
        performTransaction { ft ->
            ft.add(mContainerId, fragment, UniqueTagGenerator.generateUniqueTag(fragment))
        }
    }

    /// Performs a back navigation
    // returns false if back can't be handled locally.
    fun back(): Boolean {
        val isRootFragment = fragmentStacks[currentStackIndex].size == 1
        if (!isRootFragment) {
            popFragment()
            return true
        } else if (tabHistory.size > 1) {
            tabHistory.pop()
            switchTab(tabHistory.peek())
            delegate?.tabBarControlleDidGoBackToTabAtIndex(tabHistory.peek())
            return true
        }
        return false
    }

    fun switchTab(index: Int) {

        // Make sure the tab index is within range
        if (index >= fragmentStacks.size) {
            return
        }

        // Ask the delegate if we should respond to this selection.
        delegate?.let {
            if (!it.tabBarControlleShouldShowTabAtIndex(index)) {
                return
            }
        }

        if (currentStackIndex == index) {

            val currentFragmentStack = fragmentStacks[currentStackIndex]
            if(currentFragmentStack.size == 1){
                delegate?.tabBarClickRootFragmentAgain()
            }
            else {
                clearStack()
            }
        } else {

            val fragmentToShow = fragmentStacks[index].peek()
            performTransaction { ft ->
                // Hide Current Fragment
                val currentFragment = fragmentStacks[currentStackIndex].peek()
                ft.hide(currentFragment)

                // Show or Add next Fragment
                if (fragmentToShow.tag == null) {
                    ft.add(mContainerId, fragmentToShow, UniqueTagGenerator.generateUniqueTag(fragmentToShow))
                } else {
                    ft.show(fragmentToShow)
                }
            }
            delegate?.tabBarControllerDidShowFragment(fragmentToShow)
        }

        tabHistory.push(index)

        currentStackIndex = index
    }

    private fun performTransaction(block: (ft: FragmentTransaction) -> Unit) {
        val ft = fragmentManager.beginTransaction()
        block(ft)
        ft.commit()
        fragmentManager.executePendingTransactions()
    }

    fun pushFragment(fragment: Fragment) {
        performTransaction { ft ->

            // Hide Current fragment
            val currentFragment = fragmentStacks[currentStackIndex].peek()
            ft.hide(currentFragment)

            // Add new fragment
            ft.add(mContainerId, fragment, UniqueTagGenerator.generateUniqueTag(fragment))
        }

        /// Update Fragment stack
        fragmentStacks[currentStackIndex].push(fragment)

        delegate?.tabBarControllerDidShowFragment(fragment)
    }

    private fun popFragment() {
        val currentStack = fragmentStacks[currentStackIndex]
        val fragmentToPop = currentStack.pop()
        val fragmentToShow = currentStack.peek()
        performTransaction { ft ->
            ft.remove(fragmentToPop)
            ft.show(fragmentToShow)
        }
        delegate?.tabBarControllerDidShowFragment(fragmentToShow)
    }

    private fun clearStack() {
        val currentFragmentStack = fragmentStacks[currentStackIndex]
        val rootFragment = currentFragmentStack.first()
        performTransaction { ft ->
            while (currentFragmentStack.size > 1) {
                val fragmentToRemove = currentFragmentStack.pop()
                ft.remove(fragmentToRemove)
            }
            ft.show(rootFragment)
        }
        delegate?.tabBarControllerDidShowFragment(rootFragment)
    }

    fun clearHistory() {

        // Clear FragmentManager
        performTransaction { ft ->
            for (fragment in fragmentManager.fragments) {
                if (fragment != null) {
                    ft.remove(fragment)
                }
            }
        }

        tabHistory.clear()
    }

    override fun tabBarViewDidTapTab(tabIndex: Int) {
        switchTab(tabIndex)
    }

    fun getCurrentFragment(): Fragment {
        return fragmentStacks[currentStackIndex].last()
    }

    fun resetTabBarController(){
        UniqueTagGenerator.counter = 0
        clearHistory()
        clearStack()
    }
}
