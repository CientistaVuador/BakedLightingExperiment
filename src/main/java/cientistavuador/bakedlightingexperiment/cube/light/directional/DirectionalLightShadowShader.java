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
package cientistavuador.bakedlightingexperiment.cube.light.directional;

import cientistavuador.bakedlightingexperiment.util.BetterUniformSetter;
import cientistavuador.bakedlightingexperiment.util.ProgramCompiler;
import org.joml.Matrix4fc;

/**
 *
 * @author Cien
 */
public class DirectionalLightShadowShader {
    
    public static final String VERTEX_SHADER = 
            """
            #version 330 core
            
            uniform mat4 projectionView;
            uniform mat4 model;
            
            layout (location = 0) in vec3 vertexPosition;
            
            void main() {
                gl_Position = projectionView * model * vec4(vertexPosition, 1.0);
            }
            """;
    
    public static final String FRAGMENT_SHADER =
            """
            #version 330 core
            
            void main() {
            
            }
            """;
    
    public static final int SHADER_PROGRAM = ProgramCompiler.compile(VERTEX_SHADER, FRAGMENT_SHADER);
    public static final BetterUniformSetter UNIFORMS = new BetterUniformSetter(SHADER_PROGRAM, "projectionView", "model");
    
    public static void sendPerFrameUniforms(Matrix4fc projectionView) {
        BetterUniformSetter.uniformMatrix4fv(UNIFORMS.locationOf("projectionView"), projectionView);
    }
    
    public static void sendPerDrawUniforms(Matrix4fc model) {
        BetterUniformSetter.uniformMatrix4fv(UNIFORMS.locationOf("model"), model);
    }
    
    private DirectionalLightShadowShader() {
        
    }
}
