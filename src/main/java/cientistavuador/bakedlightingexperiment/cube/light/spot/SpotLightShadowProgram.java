/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <https://unlicense.org>
 */
package cientistavuador.bakedlightingexperiment.cube.light.spot;

import cientistavuador.bakedlightingexperiment.util.BetterUniformSetter;
import cientistavuador.bakedlightingexperiment.util.ProgramCompiler;
import org.joml.Matrix4fc;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class SpotLightShadowProgram {
    public static final String VERTEX_SHADER = 
            """
            #version 330 core
            
            uniform mat4 projectionView;
            uniform mat4 model;
            
            layout (location = 0) in vec3 vertexPosition;
            
            out vec3 position;
            
            void main() {
                vec4 worldPosition = model * vec4(vertexPosition, 1.0);
                position = worldPosition.xyz / worldPosition.w;
                gl_Position = projectionView * worldPosition;
            }
            """;
    
    public static final String FRAGMENT_SHADER =
            """
            #version 330 core
            
            uniform float nearPlane;
            uniform float farPlane;
            uniform vec3 camPos;
            
            in vec3 position;
            
            void main() {
                gl_FragDepth = (length(camPos - position) - nearPlane) / (farPlane - nearPlane);
            }
            """;
    
    public static final int SHADER_PROGRAM = ProgramCompiler.compile(VERTEX_SHADER, FRAGMENT_SHADER);
    public static final BetterUniformSetter UNIFORMS = new BetterUniformSetter(SHADER_PROGRAM, "projectionView", "model", "nearPlane", "farPlane", "camPos");
    
    public static void init() {
        
    }
    
    public static void sendPerFrameUniforms(Matrix4fc projectionView, float nearPlane, float farPlane, float camX, float camY, float camZ) {
        BetterUniformSetter.uniformMatrix4fv(UNIFORMS.locationOf("projectionView"), projectionView);
        glUniform1f(UNIFORMS.locationOf("nearPlane"), nearPlane);
        glUniform1f(UNIFORMS.locationOf("farPlane"), farPlane);
        glUniform3f(UNIFORMS.locationOf("camPos"), camX, camY, camZ);
    }
    
    public static void sendPerDrawUniforms(Matrix4fc model) {
        BetterUniformSetter.uniformMatrix4fv(UNIFORMS.locationOf("model"), model);
    }
    
    private SpotLightShadowProgram() {
        
    }
}
