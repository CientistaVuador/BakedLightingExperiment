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
import cientistavuador.bakedlightingexperiment.cube.light.Light;
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
    
    public static final int SHADOW_MAP_WIDTH = 8192;
    public static final int SHADOW_MAP_HEIGHT = 8192;
    
    private final OrthoCamera camera = new OrthoCamera();
    private final Vector3f direction = new Vector3f(-0.5f, -1f, 0.5f).normalize();
    private final Vector3f diffuseColor = new Vector3f(255f / 255f, 253f / 255f, 242f / 255f).mul(1.0f);
    private final Vector3f ambientColor = new Vector3f(255f / 255f, 253f / 255f, 242f / 255f).mul(0.3f);
    private boolean enabled = true;
    private int shadowMap = 0;
    
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
    public int getShadowMap() {
        return shadowMap;
    }
    
    @Override
    public void freeShadowMap() {
        glDeleteTextures(this.shadowMap);
        this.shadowMap = 0;
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
        this.camera.setFront(this.direction);
        
        freeShadowMap();
        
        glActiveTexture(GL_TEXTURE0);
        
        this.shadowMap = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, this.shadowMap);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32, SHADOW_MAP_WIDTH, SHADOW_MAP_HEIGHT, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_INT, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, new float[]{1f, 1f, 1f, 1f});
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
        glBindTexture(GL_TEXTURE_2D, 0);
        
        int fbo = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
        glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, this.shadowMap, 0);
        glDrawBuffers(GL_NONE);
        glReadBuffer(GL_NONE);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Could not create shadow FBO!");
        }
        
        glViewport(0, 0, SHADOW_MAP_WIDTH, SHADOW_MAP_HEIGHT);
        
        glClear(GL_DEPTH_BUFFER_BIT);
        glUseProgram(DirectionalLightShadowShader.SHADER_PROGRAM);
        
        DirectionalLightShadowShader.sendPerFrameUniforms(this.camera.getProjectionViewFloat());
        
        for (Cube c:cubes) {
            if (c.isGroundCube()) {
                glBindVertexArray(CubeVAO.GROUND_CUBE_VAO);
            } else {
                glBindVertexArray(CubeVAO.VAO);
            }
            DirectionalLightShadowShader.sendPerDrawUniforms(c.getModel());
            
            glDrawElements(GL_TRIANGLES, Cube.NUMBER_OF_INDICES, GL_UNSIGNED_INT, 0);
            glBindVertexArray(0);
        }
        
        glUseProgram(0);
        glViewport(0, 0, Main.WIDTH, Main.HEIGHT);
        
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glDeleteFramebuffers(fbo);
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
}
