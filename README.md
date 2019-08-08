# Tab bar

![Platform: Android 8+](https://img.shields.io/badge/platform-Android-68b846.svg?style=flat)
[![Language: Kotlin](https://img.shields.io/badge/language-kotlin-7963FE.svg?style=flat)](https://kotlinlang.org)
[![codebeat badge](https://codebeat.co/badges/2a5a83a4-0890-4386-af7a-325d50749e13)](https://codebeat.co/projects/github-com-pafgz-tabbar-master)
[![License: MIT](http://img.shields.io/badge/license-Apache License 2.0-lightgrey.svg?style=flat)](https://github.com/Pafgz/Tabbar/blob/master/LICENSE)

A tab bar controller to manage navigation using fragments.

## How to start

### Link the view with the controller
#### In the activity

```kotlin
class MyActivity : Activity(), TabBarController.Delegate {  
    val mTabBarController: TabBarController()
        
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      
        mTabBarView.listener = tabBarController
    }
}
```
##### Exemple of initialization
```kotlin
private fun initTabBarController() {
        mHomeFragment = HomeFragment()
        
        mOtherFragment = OtherFragment()
        
        mTabBarController = TabBarController(
                rootFragments(),
                supportFragmentManager,
                R.id.fragmentContainer)

        mTabBarController?.delegate = this

        mTabBarView.listener = mTabBarController

        tabBarControllerDidShowFragment(homeFragment)
}

private fun rootFragments(): List<Fragment> {
        return listOf(
                mHomeFragment,
                mOtherFragment)
    }
```

#### In the view
```kotlin
class TabBarView(context: Context) : LinearLayout(context) {

    var listener: TabBarViewListener? = null
```

## How to use it

### To switch tab
```kotlin
enum class TabBarState(val index: Int) {
    HOME(0),
    PROFILE(1)
}

//In the activity/fragment
tabBarController.switchTab(tabState.index)
```

### To switch fragment 
```kotlin
tabBarController.pushFragment(fragment)
```


### To manage the back button
```kotlin
override fun onBackPressed() {
        if (tabBarController != null && !tabBarController!!.back()) {
            super.onBackPressed()
        }
}
```

