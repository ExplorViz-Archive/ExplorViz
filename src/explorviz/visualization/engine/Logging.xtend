package explorviz.visualization.engine

import java.util.logging.Logger
import java.util.logging.Level

class Logging {
    val static logger = Logger::getLogger("ExplorVizLogger")
    
    def static log(String msg) {
         logger.log(Level::SEVERE, msg)
    }
    
    def static log(Throwable t) {
        log(createStackStringFromThrowable(t))
    }
    
    def static createStackStringFromThrowable(Throwable t) {
        var stack = ""
        var i = 0
        while (i < t.stackTrace.length) {
            stack = stack + "\n\t" + (t.stackTrace.get(i))
            i = i + 1
        }
        stack
    }
}
