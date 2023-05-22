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
package cientistavuador.bakedlightingexperiment.cube.light.icon;

import cientistavuador.bakedlightingexperiment.cube.light.Light;
import cientistavuador.bakedlightingexperiment.util.BetterUniformSetter;
import cientistavuador.bakedlightingexperiment.util.ProgramCompiler;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class IconProgram {
    
    public static final String VERTEX_SHADER =
            """
            #version 330 core
            
            uniform mat4 projectionView;
            uniform mat4 model;
            
            layout (location = 0) in vec3 vertexPosition;
            layout (location = 1) in vec2 vertexTexture;
            
            out vec2 texturePosition;
            
            void main() {
                texturePosition = vertexTexture;
                gl_Position = projectionView * model * vec4(vertexPosition, 1.0);
            }
            """;
    
    public static final String FRAGMENT_SHADER =
            """
            #version 330 core
            
            uniform sampler2D lightIcon;
            uniform sampler2D lightOverlay;
            
            uniform vec3 lightColor;
            
            in vec2 texturePosition;
            
            layout (location = 0) out vec4 colorOutput;
            
            void main() {
                vec4 source = texture(lightOverlay, texturePosition);
                source.rgb *= lightColor;
                vec4 dest = texture(lightIcon, texturePosition);
                
                float a = source.a + dest.a * (1.0 - source.a);
                vec3 color = (source.rgb * source.a + dest.rgb * dest.a * (1.0 - source.a)) / a;
                
                colorOutput = vec4(color, a);
            }
            """;
    
    public static final int SHADER_PROGRAM = ProgramCompiler.compile(VERTEX_SHADER, FRAGMENT_SHADER);
    private static final BetterUniformSetter UNIFORMS = new BetterUniformSetter(SHADER_PROGRAM, "projectionView", "model", "lightIcon", "lightOverlay", "lightColor");
    
    public static void init() {
        
    }
    
    public static void sendPerFrameUniforms(Matrix4fc projectionView) {
        BetterUniformSetter.uniformMatrix4fv(UNIFORMS.locationOf("projectionView"), projectionView);
        
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, IconTexture.LIGHT_OVERLAY);
        glUniform1i(UNIFORMS.locationOf("lightOverlay"), 0);
    }
    
    public static void sendPerDrawUniforms(Matrix4fc model, int iconTexture, Vector3fc lightColor) {
        BetterUniformSetter.uniformMatrix4fv(UNIFORMS.locationOf("model"), model);
        
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, iconTexture);
        glUniform1i(UNIFORMS.locationOf("lightIcon"), 1);
        
        glUniform3f(UNIFORMS.locationOf("lightColor"), lightColor.x(), lightColor.y(), lightColor.z());
    }
    
    private IconProgram() {
        
    }
    
}
