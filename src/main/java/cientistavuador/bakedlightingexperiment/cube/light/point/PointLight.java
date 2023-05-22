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

import cientistavuador.bakedlightingexperiment.Main;
import cientistavuador.bakedlightingexperiment.cube.Cube;
import cientistavuador.bakedlightingexperiment.cube.CubeVAO;
import cientistavuador.bakedlightingexperiment.cube.light.icon.IconType;
import cientistavuador.bakedlightingexperiment.cube.light.Light;
import cientistavuador.bakedlightingexperiment.cube.light.ShadowCubeMapFBO;
import java.util.List;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class PointLight implements Light {

    public static final float NEAR_PLANE = 0.01f;
    public static final float FAR_PLANE = 1000f;
    
    private final Vector3f position;
    private final Vector3f diffuseColor;
    private final Vector3f ambientColor;
    private final Vector3f iconColor;
    private boolean enabled = true;

    public PointLight(Vector3fc position, Vector3fc color, float brightness) {
        this.position = new Vector3f(position);
        this.diffuseColor = new Vector3f(color).mul(brightness);
        this.ambientColor = new Vector3f(color).mul(brightness / 32f);
        this.iconColor = new Vector3f(color);
    }

    @Override
    public Vector3f getIconColor() {
        return this.iconColor;
    }

    @Override
    public Vector3f getPosition() {
        return position;
    }

    @Override
    public Vector3f getAmbientColor() {
        return ambientColor;
    }

    @Override
    public Vector3f getDiffuseColor() {
        return diffuseColor;
    }

    @Override
    public void render(Cube cube, int lightmap) {
        glUseProgram(PointLightProgram.SHADER_PROGRAM);
        glBindVertexArray(Cube.VAO);

        PointLightProgram.sendUniforms(lightmap, cube.getModel(), cube.getNormalModel(), this);
        glDrawElements(GL_TRIANGLES, Cube.NUMBER_OF_INDICES, GL_UNSIGNED_INT, 0);

        Main.NUMBER_OF_DRAWCALLS++;
        Main.NUMBER_OF_VERTICES += Cube.NUMBER_OF_INDICES;

        glBindVertexArray(0);
        glUseProgram(0);
    }

    @Override
    public void renderShadowMap(List<Cube> cubes) {
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, ShadowCubeMapFBO.fbo());
        glViewport(0, 0, ShadowCubeMapFBO.size(), ShadowCubeMapFBO.size());

        glClear(GL_DEPTH_BUFFER_BIT);
        glUseProgram(PointLightShadowProgram.SHADER_PROGRAM);

        PointLightShadowProgram.sendPerFrameUniforms(
                NEAR_PLANE,
                FAR_PLANE,
                this.position.x(),
                this.position.y(),
                this.position.z()
        );

        for (Cube c : cubes) {
            if (c.isGroundCube()) {
                glBindVertexArray(CubeVAO.GROUND_CUBE_VAO);
            } else {
                glBindVertexArray(CubeVAO.VAO);
            }
            PointLightShadowProgram.sendPerDrawUniforms(c.getModel());

            glDrawElements(GL_TRIANGLES, Cube.NUMBER_OF_INDICES, GL_UNSIGNED_INT, 0);
            glBindVertexArray(0);
        }

        glUseProgram(0);
        glViewport(0, 0, Main.WIDTH, Main.HEIGHT);

        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public IconType getIconType() {
        return IconType.POINT;
    }
}
