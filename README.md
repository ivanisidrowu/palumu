# Palumu
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
val floatingViewHelper = FloatingViewHelper()
floatingViewHelper.floatingView = floatingPlayer
floatingViewHelper.recyclerView = recyclerView
floatingViewHelper.listener = listener
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
val videoPageFrame = ScalablePageFrame(context)
videoPageFrame.init(headView, bodyView, root)
```
It also supports fullscreen, just add few lines of code into onConfigurationChanged.

![](https://github.com/ivanisidrowu/palumu/blob/master/demo/palumu-page-fullscreen.gif)

```kotlin
// Enter fullscreen (use it in activity onConfigurationChanged)
videoPageFrame?.enterFullScreen()
// Leave fullscreen (use it in activity onConfigurationChanged)
videoPageFrame?.leaveFullScreen()
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
  compile 'com.github.ivanisidrowu:palumu:0.9.1'
}
```
## Contribution
Contributions are always welcome. If you have any ideas or suggestions, you can contact me or create a github issue.

