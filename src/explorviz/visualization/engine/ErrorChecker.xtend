package explorviz.visualization.engine

import elemental.html.WebGLRenderingContext

class ErrorChecker {
    static WebGLRenderingContext glContext

    def static init(WebGLRenderingContext glContextParam) {
        glContext = glContextParam
    }

    def static checkErrors() {
        val error = glContext.getError()
        if (error != WebGLRenderingContext::NO_ERROR) {
            var errorDesc = "UNKNOWN"
            if (error == WebGLRenderingContext::INVALID_ENUM) {
                errorDesc = "INVALID_ENUM"
            } else if (error == WebGLRenderingContext::INVALID_OPERATION) {
                errorDesc = "INVALID_OPERATION"
            } else if (error == WebGLRenderingContext::INVALID_FRAMEBUFFER_OPERATION) {
                errorDesc = "INVALID_FRAMEBUFFER_OPERATION"
            } else if (error == WebGLRenderingContext::OUT_OF_MEMORY) {
                errorDesc = "OUT_OF_MEMORY"
            }
            val message = "WebGL Error: " + error + ", " + errorDesc
            println(message)
            throw new RuntimeException(message)
        }
    }
}
