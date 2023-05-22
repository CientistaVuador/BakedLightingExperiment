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

import cientistavuador.bakedlightingexperiment.cube.light.ShadowMap2DFBO;
import cientistavuador.bakedlightingexperiment.util.ProgramCompiler;
import java.nio.FloatBuffer;
import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import static org.lwjgl.opengl.GL33C.*;
import org.lwjgl.system.MemoryStack;

/**
 *
 * @author Cien
 */
public class DirectionalLightProgram {
    public static final String VERTEX_SHADER
            = 
            """
            #version 330 core
            
            uniform mat4 model;
            uniform mat3 normalModel;
            
            layout (location = 0) in vec3 vertexPosition;
            layout (location = 1) in vec3 vertexNormal;
            //
            layout (location = 3) in vec2 vertexLightmap;
            layout (location = 4) in vec2 vertexLightmapPosition;
            
            out vec3 position;
            out vec2 texCoords;
            out vec3 normal;
            
            void main() {
                vec4 pos = model * vec4(vertexPosition, 1.0);
                pos.xyz /= pos.w;
                position = pos.xyz;
                
                texCoords = (vertexLightmapPosition + 1.0) / 2.0;
                normal = normalize(normalModel * vertexNormal);
                
                gl_Position = vec4(vertexLightmapPosition, 1.0, 1.0);
            }
            """;

    public static final String FRAGMENT_SHADER
            = 
            """
            #version 330 core
            
            uniform sampler2D lightmapTexture;
            uniform sampler2DShadow shadowMap;
            uniform mat4 shadowMapProjectionView;
            
            uniform vec3 lightDirection;
            uniform vec3 lightAmbient;
            uniform vec3 lightDiffuse;
            
            in vec3 position;
            in vec2 texCoords;
            in vec3 normal;
            
            layout (location = 0) out vec4 outputColor;
            
            void main() {
                vec2 shadowMapTexelSize = 1.0 / vec2(textureSize(shadowMap, 0));
                int pcf = 3;
                
                vec4 mapCoords = shadowMapProjectionView * vec4(position, 1.0);
                mapCoords /= mapCoords.w;
                mapCoords.xyz = (mapCoords.xyz + 1.0) / 2.0;
                
                float shadowValue = 0.0;
                for (int x = -pcf; x <= pcf; x++) {
                    for (int y = -pcf; y <= pcf; y++) {
                        shadowValue += texture(shadowMap, vec3(mapCoords.xy + (vec2(float(x), float(y)) * shadowMapTexelSize), mapCoords.z - 0.0002));
                    }
                }
                shadowValue /= pow((float(pcf) * 2.0) + 1.0, 2.0);
                
                vec3 resultColor = vec3(0.0);
                
                resultColor += lightDiffuse * max(dot(normal, -lightDirection), 0.0) * shadowValue;
                resultColor += lightAmbient;
                
                ivec2 lightmapPixel = ivec2(floor(texCoords * vec2(textureSize(lightmapTexture, 0))));
                outputColor = vec4(resultColor + texelFetch(lightmapTexture, lightmapPixel, 0).rgb, 1.0);
            }
            """;

    public static final int SHADER_PROGRAM = ProgramCompiler.compile(VERTEX_SHADER, FRAGMENT_SHADER);
    public static final int MODEL_INDEX = glGetUniformLocation(SHADER_PROGRAM, "model");
    public static final int NORMAL_MODEL_INDEX = glGetUniformLocation(SHADER_PROGRAM, "normalModel");
    public static final int LIGHTMAP_TEXTURE_INDEX = glGetUniformLocation(SHADER_PROGRAM, "lightmapTexture");
    
    public static final int LIGHT_DIRECTION_INDEX = glGetUniformLocation(SHADER_PROGRAM, "lightDirection");
    public static final int LIGHT_AMBIENT_INDEX = glGetUniformLocation(SHADER_PROGRAM, "lightAmbient");
    public static final int LIGHT_DIFFUSE_INDEX = glGetUniformLocation(SHADER_PROGRAM, "lightDiffuse");
    
    public static final int SHADOW_MAP_INDEX = glGetUniformLocation(SHADER_PROGRAM, "shadowMap");
    public static final int SHADOW_MAP_PROJECTION_VIEW_INDEX = glGetUniformLocation(SHADER_PROGRAM, "shadowMapProjectionView");
    
    private static void sendMatrix(int location, Matrix4fc matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer matrixBuffer = stack.mallocFloat(4 * 4);
            matrix.get(matrixBuffer);
            glUniformMatrix4fv(location, false, matrixBuffer);
        }
    }
    
    private static void sendMatrix3f(int location, Matrix3fc matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer matrixBuffer = stack.mallocFloat(3 * 3);
            matrix.get(matrixBuffer);
            glUniformMatrix3fv(location, false, matrixBuffer);
        }
    }
    
    public static void init() {
        DirectionalLightShadowProgram.init();
    }

    public static void sendUniforms(int lightmapTexture, Matrix4fc model, Matrix3fc normalModel, DirectionalLight light) {
        sendMatrix(MODEL_INDEX, model);
        sendMatrix3f(NORMAL_MODEL_INDEX, normalModel);
        
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, lightmapTexture);
        glUniform1i(LIGHTMAP_TEXTURE_INDEX, 0);
        
        glUniform3f(LIGHT_DIRECTION_INDEX, light.getDirection().x(), light.getDirection().y(), light.getDirection().z());
        glUniform3f(LIGHT_AMBIENT_INDEX, light.getAmbientColor().x(), light.getAmbientColor().y(), light.getAmbientColor().z());
        glUniform3f(LIGHT_DIFFUSE_INDEX, light.getDiffuseColor().x(), light.getDiffuseColor().y(), light.getDiffuseColor().z());
        
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, ShadowMap2DFBO.shadowMap());
        glUniform1i(SHADOW_MAP_INDEX, 1);
        sendMatrix(SHADOW_MAP_PROJECTION_VIEW_INDEX, light.getCamera().getProjectionViewFloat());
    }
    
    private DirectionalLightProgram() {

    }
}
