# Palumu

[![Jitpack](https://jitpack.io/v/ivanisidrowu/palumu.svg)](https://jitpack.io/#ivanisidrowu/palumu)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Palulmu-blue.svg?style=flat)](https://android-arsenal.com/details/1/6898)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A floating view helper lib to let you create floating view above other views!

* Floating view can follow specified view
* Scalable view supported
* Fullscreen supported

![](https://github.com/ivanisidrowu/palumu/blob/master/demo/palumu-demo.gif)

## Components
### FloatingViewHelper

![](https://github.com/ivanisidrowu/palumu/blob/master/demo/palumu-floating.gif)

Here is an example of how to create a floating view above a RecyclerView
```kotlin
val floatingViewHelper = FloatingViewHelper().apply {
  floatingView = floatingPlayer
  recyclerView = recyclerView
  listener = listener
}
```
The listener is needed for helper to get the view followed by floating view.
Please make sure you override getTargetView() in FloatingViewListener.

Then, you attach the view and start the helper.
```kotlin
floatingViewHelper.attachFloatingView()
floatingViewHelper.start()
```

Fullscreen is supported.

![](https://github.com/ivanisidrowu/palumu/blob/master/demo/palumu-list-fullscreen.gif)

```kotlin
// Enter fullscreen (use it in activity onConfigurationChanged)
floatingViewHelper.enterFullScreen()

// Leave fullscreen (use it in activity onConfigurationChanged)
floatingViewHelper.leaveFullScreen()
```
Other details could be found in ![wiki](https://github.com/ivanisidrowu/palumu/wiki/Document). You can also refer to ![the sample APP](https://github.com/ivanisidrowu/palumu/tree/master/app/src/main).

### ScalablePageFrame

This class provides youtubish style view. It can be scaled and swipe-to-close. You can set head view as the upper part of the UI and body view as lower part of the UI. In general, you can set any views to head and body. But SurfaceView now is not supported.

Add the frame into the specified view.
```kotlin
val videoPageFrame = ScalablePageFrame(context).apply {
  setHead(headView) // set head view or fragments
  setBody(bodyFragment) // set body view or fragments
  attach(root) // attach frame to root view
}
```
It also supports fullscreen, just add few lines of code into onConfigurationChanged.

![](https://github.com/ivanisidrowu/palumu/blob/master/demo/palumu-page-fullscreen.gif)

```kotlin
// Enter fullscreen (use it in activity onConfigurationChanged)
videoPageFrame?.enterFullScreen()
// Leave fullscreen (use it in activity onConfigurationChanged)
videoPageFrame?.leaveFullScreen()
```

Be careful! Don't forget to detach the frame when destroying the activity or fragment.
```kotlin
override fun onDestroy() {
        super.onDestroy()
        videoPageFrame?.detach()
}
```

Other details could be found in ![wiki](https://github.com/ivanisidrowu/palumu/wiki/Document). You can also refer to ![the sample APP](https://github.com/ivanisidrowu/palumu/tree/master/app/src/main).

### DraggablePageFrame

It provides a frame container that can be maximized, minimized, and dragged on the screen.

<img src="https://github.com/ivanisidrowu/palumu/blob/master/demo/draggable-demo.gif" width="216" height="384">

The example below shows you how to initialize a `DraggablePageFrame`.

```kotlin
val pageFrame = DraggablePageFrame(this).apply {
            setContentView(imageView)
            val margin = resources.getDimensionPixelSize(R.dimen.item_margin)
            setContentMargin(margin)
            attach(root)
        }
```

You can also set a `FrameListener` into it.
```kotlin
pageFrame.listener = object : FrameListener {
    override fun onMinimized() {
       // Do it!
    }
}
```

Be careful! Don't forget to detach the frame when destroying the activity or fragment.
```kotlin
override fun onDestroy() {
        super.onDestroy()
        pageFrame.detach()
}
```

Other details could be found in ![wiki](https://github.com/ivanisidrowu/palumu/wiki/Document). You can also refer to ![the sample APP](https://github.com/ivanisidrowu/palumu/tree/master/app/src/main).

## Download
Add this repo to the root build.gradle file.
```gradle
allprojects {
  repositories {
  ...
  maven { url 'https://jitpack.io' }
  }
}
```
Then add this dependency to app's build.gradle file.
```gradle
dependencies {
  compile 'com.github.ivanisidrowu:palumu:1.0.0'
}
```
## Contribution
Contributions are always welcome. If you have any ideas or suggestions, you can contact me or create a github issue.
