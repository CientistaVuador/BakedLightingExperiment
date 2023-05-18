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
package cientistavuador.bakedlightingexperiment.cube;

import cientistavuador.bakedlightingexperiment.util.ProgramCompiler;
import java.nio.FloatBuffer;
import org.joml.Matrix4fc;
import static org.lwjgl.opengl.GL33C.*;
import org.lwjgl.system.MemoryStack;

/**
 *
 * @author Cien
 */
public class CubeProgram {

    public static final String VERTEX_SHADER
            = 
            """
            #version 330 core
            
            uniform mat4 projectionView;
            uniform mat4 model;
            
            layout (location = 0) in vec3 vertexPosition;
            //
            layout (location = 2) in vec2 vertexTexture;
            layout (location = 3) in vec2 vertexLightmap;
            
            out vec2 texCoords;
            out vec2 texCoordsLightmap;
            
            void main() {
                texCoords = vertexTexture;
                texCoordsLightmap = vertexLightmap;
                gl_Position = projectionView * model * vec4(vertexPosition, 1.0);
            }
            """;

    public static final String FRAGMENT_SHADER
            = 
            """
            #version 330 core
            
            uniform sampler2D cubeTexture;
            uniform sampler2D lightmapTexture;
            
            layout (location = 0) out vec4 outputColor;
            
            in vec2 texCoords;
            in vec2 texCoordsLightmap;
            
            void main() {
                vec4 textureColor = texture(cubeTexture, texCoords);
                
                textureColor.rgb = pow(textureColor.rgb, vec3(2.2));
                
                vec3 lightColor = texture(lightmapTexture, texCoordsLightmap).rgb;
                textureColor.rgb *= lightColor;
                
                textureColor.rgb = pow(textureColor.rgb, vec3(1.0/2.2));
                
                outputColor = textureColor;
            }
            """;

    public static final int SHADER_PROGRAM = ProgramCompiler.compile(VERTEX_SHADER, FRAGMENT_SHADER);
    public static final int PROJECTION_VIEW_INDEX = glGetUniformLocation(SHADER_PROGRAM, "projectionView");
    public static final int MODEL_INDEX = glGetUniformLocation(SHADER_PROGRAM, "model");
    public static final int CUBE_TEXTURE_INDEX = glGetUniformLocation(SHADER_PROGRAM, "cubeTexture");
    public static final int LIGHTMAP_TEXTURE_INDEX = glGetUniformLocation(SHADER_PROGRAM, "lightmapTexture");
    
    private static void sendMatrix(int location, Matrix4fc matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer matrixBuffer = stack.mallocFloat(4 * 4);
            matrix.get(matrixBuffer);
            glUniformMatrix4fv(location, false, matrixBuffer);
        }
    }

    public static void sendPerFrameUniforms(int cubeTexture, Matrix4fc projectionView) {
        sendMatrix(PROJECTION_VIEW_INDEX, projectionView);
        
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, cubeTexture);
        glUniform1i(CUBE_TEXTURE_INDEX, 0);
    }

    public static void sendPerDrawUniforms(int lightmapTexture, Matrix4fc model) {
        sendMatrix(MODEL_INDEX, model);
        
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, lightmapTexture);
        glUniform1i(LIGHTMAP_TEXTURE_INDEX, 1);
    }
    
    private CubeProgram() {

    }

}
