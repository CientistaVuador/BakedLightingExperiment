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
package cientistavuador.bakedlightingexperiment.cube.light.point;

import cientistavuador.bakedlightingexperiment.util.BetterUniformSetter;
import cientistavuador.bakedlightingexperiment.util.ProgramCompiler;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import static org.lwjgl.opengl.GL20C.glUniform1f;
import static org.lwjgl.opengl.GL20C.glUniform3f;

/**
 *
 * @author Cien
 */
public class PointLightShadowProgram {

    public static final String VERTEX_SHADER
            = 
            """
            #version 330 core
            
            uniform mat4 model;
            
            layout (location = 0) in vec3 vertexPosition;
            
            void main() {
                gl_Position = model * vec4(vertexPosition, 1.0);
            }
            """;

    public static final String GEOMETRY_SHADER
            = 
            """
            #version 330 core
            
            layout (triangles) in;
            layout (triangle_strip, max_vertices=18) out;
            
            uniform mat4 projectionView[6];
            
            out vec3 position;
            
            void main() {
                for (int i = 0; i < 6; i++) {
                    gl_Layer = i;
                    for (int j = 0; j < 3; j++) {
                        vec4 pos = gl_in[j].gl_Position;
                        pos /= pos.w;
                        position = pos.xyz;
                        gl_Position = projectionView[i] * pos;
                        EmitVertex();
                    }
                    EndPrimitive();
                }
            }
            """;

    public static final String FRAGMENT_SHADER
            = 
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

    public static final int SHADER_PROGRAM = ProgramCompiler.compile(VERTEX_SHADER, GEOMETRY_SHADER, FRAGMENT_SHADER);
    public static final BetterUniformSetter UNIFORMS = new BetterUniformSetter(SHADER_PROGRAM, "model", "nearPlane", "farPlane", "camPos", "projectionView[0]", "projectionView[1]", "projectionView[2]", "projectionView[3]", "projectionView[4]", "projectionView[5]");

    public static void sendPerFrameUniforms(float nearPlane, float farPlane, float camX, float camY, float camZ) {
        glUniform1f(UNIFORMS.locationOf("nearPlane"), nearPlane);
        glUniform1f(UNIFORMS.locationOf("farPlane"), farPlane);
        glUniform3f(UNIFORMS.locationOf("camPos"), camX, camY, camZ);

        Matrix4f projection = new Matrix4f().perspective(
                (float) Math.toRadians(90f),
                1f / 1f,
                nearPlane,
                farPlane
        );

        BetterUniformSetter.uniformMatrix4fv(UNIFORMS.locationOf("projectionView[0]"),
                projection.mul(
                        new Matrix4f()
                                .lookAt(
                                        camX, camY, camZ,
                                        camX + 1f, camY + 0f, camZ + 0f,
                                        0f, -1f, 0f
                                ),
                        new Matrix4f()
                )
        );

        BetterUniformSetter.uniformMatrix4fv(UNIFORMS.locationOf("projectionView[1]"),
                projection.mul(
                        new Matrix4f()
                                .lookAt(
                                        camX, camY, camZ,
                                        camX + -1f, camY + 0f, camZ + 0f,
                                        0f, -1f, 0f
                                ),
                        new Matrix4f()
                )
        );

        BetterUniformSetter.uniformMatrix4fv(UNIFORMS.locationOf("projectionView[2]"),
                projection.mul(
                        new Matrix4f()
                                .lookAt(
                                        camX, camY, camZ,
                                        camX + 0f, camY + 1f, camZ + 0f,
                                        0f, 0f, 1f
                                ),
                        new Matrix4f()
                )
        );

        BetterUniformSetter.uniformMatrix4fv(UNIFORMS.locationOf("projectionView[3]"),
                projection.mul(
                        new Matrix4f()
                                .lookAt(
                                        camX, camY, camZ,
                                        camX + 0f, camY + -1f, camZ + 0f,
                                        0f, 0f, -1f
                                ),
                        new Matrix4f()
                )
        );

        BetterUniformSetter.uniformMatrix4fv(UNIFORMS.locationOf("projectionView[4]"),
                projection.mul(
                        new Matrix4f()
                                .lookAt(
                                        camX, camY, camZ,
                                        camX + 0f, camY + 0f, camZ + 1f,
                                        0f, -1f, 0f
                                ),
                        new Matrix4f()
                )
        );

        BetterUniformSetter.uniformMatrix4fv(UNIFORMS.locationOf("projectionView[5]"),
                projection.mul(
                        new Matrix4f()
                                .lookAt(
                                        camX, camY, camZ,
                                        camX + 0f, camY + 0f, camZ + -1f,
                                        0f, -1f, 0f
                                ),
                        new Matrix4f()
                )
        );
    }

    public static void init() {
        
    }
    
    public static void sendPerDrawUniforms(Matrix4fc model) {
        BetterUniformSetter.uniformMatrix4fv(UNIFORMS.locationOf("model"), model);
    }

    private PointLightShadowProgram() {

    }
}
