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

import cientistavuador.bakedlightingexperiment.Main;
import static cientistavuador.bakedlightingexperiment.Main.DEFAULT_CLEAR_COLOR;
import cientistavuador.bakedlightingexperiment.cube.light.Light;
import cientistavuador.bakedlightingexperiment.cube.light.ShadowCubeMapFBO;
import cientistavuador.bakedlightingexperiment.cube.light.ShadowMap2DFBO;
import cientistavuador.bakedlightingexperiment.cube.light.directional.DirectionalLightProgram;
import cientistavuador.bakedlightingexperiment.cube.light.point.PointLightProgram;
import cientistavuador.bakedlightingexperiment.cube.light.spot.SpotLightProgram;
import java.nio.ByteBuffer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import static org.lwjgl.opengl.GL33C.*;
import org.lwjgl.system.MemoryUtil;

/**
 *
 * @author Cien
 */
public class Cube {

    public static final int VERTEX_SIZE_ELEMENTS = 3 + 3 + 2 + 2 + 2;
    public static final int NUMBER_OF_INDICES = 3 * 2 * 6;
    public static final int CUBE_TEXTURE = CubeTexture.CUBE_TEXTURE;
    public static final int SHADER_PROGRAM = CubeProgram.SHADER_PROGRAM;
    public static final int VAO = CubeVAO.VAO;

    public static void init() {
        DirectionalLightProgram.init();
        PointLightProgram.init();
        SpotLightProgram.init();
        ShadowCubeMapFBO.init();
        ShadowMap2DFBO.init();
    }

    private final Matrix4f model = new Matrix4f();
    private final Matrix3f normalModel = new Matrix3f();
    private final boolean groundCube;
    private final int fbo = glGenFramebuffers();
    private final int lightmap = glGenTextures();
    private final int auxTexture = glGenTextures();
    
    private int readLightmap = this.lightmap;
    private int drawLightmap = this.auxTexture;
    private int attachment = GL_COLOR_ATTACHMENT1;

    public Cube(Matrix4fc model, boolean groundCube) {
        this.groundCube = groundCube;
        this.model.set(model);
        this.normalModel.set(new Matrix4f(model).invert().transpose());

        int width = CubeVAO.getLightmapWidth();
        int height = CubeVAO.getLightmapHeight();

        if (groundCube) {
            width = CubeVAO.GROUND_CUBE_WIDTH;
            height = CubeVAO.GROUND_CUBE_HEIGHT;
        }

        ByteBuffer mem = MemoryUtil.memCalloc((width * height) * 3);
        MemoryUtil.memSet(mem, 255);

        glActiveTexture(GL_TEXTURE0);

        //create lightmap
        glBindTexture(GL_TEXTURE_2D, this.lightmap);
        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_R11F_G11F_B10F,
                width,
                height,
                0,
                GL_RGB,
                GL_UNSIGNED_BYTE,
                mem
        );
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glBindTexture(GL_TEXTURE_2D, 0);

        MemoryUtil.memFree(mem);

        //create auxTexture
        glBindTexture(GL_TEXTURE_2D, this.auxTexture);
        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_R11F_G11F_B10F,
                width,
                height,
                0,
                GL_RGB,
                GL_UNSIGNED_BYTE,
                0
        );
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glBindTexture(GL_TEXTURE_2D, 0);

        //create fbo
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);

        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, this.lightmap, 0);
        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, this.auxTexture, 0);
        
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Could not create FBO!");
        }
        
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public boolean isGroundCube() {
        return groundCube;
    }

    public Matrix4f getModel() {
        return model;
    }

    public Matrix3f getNormalModel() {
        return normalModel;
    }

    public int getLightmap() {
        return this.readLightmap;
    }

    public void clearLightmap() {
        int width = CubeVAO.getLightmapWidth();
        int height = CubeVAO.getLightmapHeight();

        if (isGroundCube()) {
            width = CubeVAO.GROUND_CUBE_WIDTH;
            height = CubeVAO.GROUND_CUBE_HEIGHT;
        }

        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, this.fbo);
        glDrawBuffers(new int[]{GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1});

        glViewport(0, 0, width, height);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        glClear(GL_COLOR_BUFFER_BIT);

        glClearColor(DEFAULT_CLEAR_COLOR.x(), DEFAULT_CLEAR_COLOR.y(), DEFAULT_CLEAR_COLOR.z(), 1.0f);
        glViewport(0, 0, Main.WIDTH, Main.HEIGHT);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
    }

    public void updateLightmap(Light light) {
        if (!light.isEnabled()) {
            return;
        }
        
        int width = CubeVAO.getLightmapWidth();
        int height = CubeVAO.getLightmapHeight();

        if (isGroundCube()) {
            width = CubeVAO.GROUND_CUBE_WIDTH;
            height = CubeVAO.GROUND_CUBE_HEIGHT;
        }

        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, this.fbo);
        glDrawBuffers(new int[]{this.attachment});
        glViewport(0, 0, width, height);
        
        light.render(this, this.readLightmap);

        int a = this.readLightmap;
        int b = this.drawLightmap;
        this.readLightmap = b;
        this.drawLightmap = a;
        this.attachment = (this.attachment == GL_COLOR_ATTACHMENT0 ? GL_COLOR_ATTACHMENT1 : GL_COLOR_ATTACHMENT0);
        
        glViewport(0, 0, Main.WIDTH, Main.HEIGHT);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
    }

    public void free() {
        glDeleteFramebuffers(this.fbo);
        glDeleteTextures(this.auxTexture);
        glDeleteTextures(this.lightmap);
    }

}
