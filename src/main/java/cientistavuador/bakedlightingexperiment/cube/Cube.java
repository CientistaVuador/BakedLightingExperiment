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
    public static final int SHADER_PROGRAM_LIGHTING = CubeLightingProgram.SHADER_PROGRAM;
    public static final int VAO = CubeVAO.VAO;
    
    public static void init() {
        
    }
    
    private final Matrix4f model = new Matrix4f();
    private final Matrix3f normalModel = new Matrix3f();
    private final boolean groundCube;
    private int lightmap = glGenTextures();
    
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
        
        ByteBuffer mem = MemoryUtil.memCalloc((width*height) * 3);
        MemoryUtil.memSet(mem, 255);
        
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, this.lightmap);
        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RGB8,
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
        return lightmap;
    }
    
    public void updateLightmap(DirectionalLight sun) {
        int width = CubeVAO.getLightmapWidth();
        int height = CubeVAO.getLightmapHeight();
        
        if (isGroundCube()) {
            width = CubeVAO.GROUND_CUBE_WIDTH;
            height = CubeVAO.GROUND_CUBE_HEIGHT;
        }
        
        glActiveTexture(GL_TEXTURE0);
        
        //clear current texture
        glBindTexture(GL_TEXTURE_2D, this.lightmap);
        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RGB8,
                width,
                height,
                0,
                GL_RGB,
                GL_UNSIGNED_BYTE,
                0
        );
        glBindTexture(GL_TEXTURE_2D, 0);
        
        //create auxTexture
        int auxTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, auxTexture);
        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RGB8,
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
        
        //create fbo, set main and aux texture as targets, no depth buffer required.
        int fbo = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
        
        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, this.lightmap, 0);
        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, auxTexture, 0);
        
        glDrawBuffers(new int[] {GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1});
        glReadBuffer(GL_NONE);
        
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Could not create FBO!");
        }
        
        glViewport(0, 0, width, height);
        
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);
        
        glDrawBuffers(new int[] {GL_COLOR_ATTACHMENT0});
        
        glUseProgram(CubeLightingProgram.SHADER_PROGRAM);
        glBindVertexArray(Cube.VAO);
        
        CubeLightingProgram.sendUniforms(auxTexture, this.model, this.normalModel, sun);
        glDrawElements(GL_TRIANGLES, Cube.NUMBER_OF_INDICES, GL_UNSIGNED_INT, 0);
        
        Main.NUMBER_OF_DRAWCALLS++;
        Main.NUMBER_OF_VERTICES += Cube.NUMBER_OF_INDICES;
        
        glBindVertexArray(0);
        glUseProgram(0);
        
        glViewport(0, 0, Main.WIDTH, Main.HEIGHT);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        
        glClearColor(0.2f, 0.4f, 0.6f, 1.0f);
        
        glDeleteFramebuffers(fbo);
        glDeleteTextures(auxTexture);
        this.lightmap = this.lightmap;
    }
    
    public void free() {
        glDeleteTextures(this.lightmap);
    }
    
}
