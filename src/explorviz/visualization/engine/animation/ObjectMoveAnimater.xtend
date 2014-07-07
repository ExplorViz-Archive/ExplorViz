package explorviz.visualization.engine.animation

import java.util.Date

class ObjectMoveAnimater {
    var static boolean currentlyAnimating = false
    var static long animationStarttime = 0
    val static float ANIMATION_DURATION_IN_MSEC = 600f

    def static startAnimation() {
        animationStarttime = new Date().time
        currentlyAnimating = true
    }

    def static stopAnimation() {
        currentlyAnimating = false
        animationStarttime = 0
    }

    def static float getAnimationTimePassedPercent() {
        var timePassedInPercent = 1f
        if (currentlyAnimating) {
            val currentTimeDiff = new Date().time - animationStarttime
            if (currentTimeDiff > ANIMATION_DURATION_IN_MSEC) {
                stopAnimation()
            } else {
                timePassedInPercent = currentTimeDiff / ANIMATION_DURATION_IN_MSEC
            }
        }

        timePassedInPercent
    }
}
