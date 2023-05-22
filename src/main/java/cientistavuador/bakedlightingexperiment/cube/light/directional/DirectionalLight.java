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

import cientistavuador.bakedlightingexperiment.Main;
import cientistavuador.bakedlightingexperiment.camera.OrthoCamera;
import cientistavuador.bakedlightingexperiment.cube.Cube;
import cientistavuador.bakedlightingexperiment.cube.CubeVAO;
import cientistavuador.bakedlightingexperiment.cube.light.icon.IconType;
import cientistavuador.bakedlightingexperiment.cube.light.Light;
import cientistavuador.bakedlightingexperiment.cube.light.ShadowMap2DFBO;
import java.util.List;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class DirectionalLight implements Light {
    
    private static final Vector3f position = new Vector3f(0f);
    
    private final OrthoCamera camera = new OrthoCamera();
    private final Vector3f direction = new Vector3f(-0.5f, -1f, 0.5f).normalize();
    private final Vector3f iconColor = new Vector3f(255f / 255f, 253f / 255f, 242f / 255f);
    private final Vector3f diffuseColor = new Vector3f(iconColor).mul(1.0f);
    private final Vector3f ambientColor = new Vector3f(iconColor).mul(0.3f);
    private boolean enabled = true;
    
    public DirectionalLight(Vector3fc direction) {
        if (direction != null) {
            this.direction.set(direction);
        }
        camera.setFront(this.direction);
        camera.setPosition(
                new Vector3d()
                .set(this.direction)
                .negate()
                .mul(200f)
                .add(0f, 0f, 0f)
        );
        camera.setDimensions(100, 100);
        camera.setNearPlane(0f);
        camera.setFarPlane(500f);
    }
    
    public DirectionalLight() {
        this(null);
    }

    @Override
    public Vector3f getIconColor() {
        return this.iconColor;
    }
    
    @Override
    public Vector3fc getPosition() {
        return DirectionalLight.position;
    }
    
    public OrthoCamera getCamera() {
        return camera;
    }
    
    public Vector3f getDirection() {
        return direction;
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
        glUseProgram(DirectionalLightProgram.SHADER_PROGRAM);
        glBindVertexArray(Cube.VAO);
        
        DirectionalLightProgram.sendUniforms(lightmap, cube.getModel(), cube.getNormalModel(), this);
        glDrawElements(GL_TRIANGLES, Cube.NUMBER_OF_INDICES, GL_UNSIGNED_INT, 0);
        
        Main.NUMBER_OF_DRAWCALLS++;
        Main.NUMBER_OF_VERTICES += Cube.NUMBER_OF_INDICES;
        
        glBindVertexArray(0);
        glUseProgram(0);
    }
    
    @Override
    public void renderShadowMap(List<Cube> cubes) {
        ShadowMap2DFBO.updateShadowMapSize(ShadowMap2DFBO.DEFAULT_WIDTH, ShadowMap2DFBO.DEFAULT_HEIGHT);
        
        this.camera.setFront(this.direction);
        
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, ShadowMap2DFBO.fbo());
        glViewport(0, 0, ShadowMap2DFBO.width(), ShadowMap2DFBO.height());
        
        glClear(GL_DEPTH_BUFFER_BIT);
        glUseProgram(DirectionalLightShadowProgram.SHADER_PROGRAM);
        
        DirectionalLightShadowProgram.sendPerFrameUniforms(this.camera.getProjectionViewFloat());
        
        for (Cube c:cubes) {
            if (c.isGroundCube()) {
                glBindVertexArray(CubeVAO.GROUND_CUBE_VAO);
            } else {
                glBindVertexArray(CubeVAO.VAO);
            }
            DirectionalLightShadowProgram.sendPerDrawUniforms(c.getModel());
            
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
        return IconType.NONE;
    }
    
}
