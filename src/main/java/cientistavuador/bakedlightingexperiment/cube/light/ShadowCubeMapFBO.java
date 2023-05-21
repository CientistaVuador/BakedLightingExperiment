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
package cientistavuador.bakedlightingexperiment.cube.light;

import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class ShadowCubeMapFBO {
    public static final int DEFAULT_SIZE = 1536;
    
    private static int size = DEFAULT_SIZE;
    private static final int shadowCubeMap = glGenTextures();
    private static final int fbo = glGenFramebuffers();
    
    static {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, ShadowCubeMapFBO.shadowCubeMap);
        for (int i = 0; i < 6; i++) {
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_DEPTH_COMPONENT32, ShadowCubeMapFBO.size, ShadowCubeMapFBO.size, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_INT, 0);
        }
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
        
        glBindFramebuffer(GL_FRAMEBUFFER, ShadowCubeMapFBO.fbo);
        glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, ShadowCubeMapFBO.shadowCubeMap, 0);
        glDrawBuffers(GL_NONE);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Could not create shadow FBO!");
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
    
    public static void updateShadowCubeMapSize(int size) {
        if (ShadowCubeMapFBO.size == size) {
            return;
        }
        
        ShadowCubeMapFBO.size = size;
        ShadowCubeMapFBO.size = size;
        
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, ShadowCubeMapFBO.shadowCubeMap);
        for (int i = 0; i < 6; i++) {
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_DEPTH_COMPONENT32, ShadowCubeMapFBO.size, ShadowCubeMapFBO.size, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_INT, 0);
        }
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
    }
    
    public static int size() {
        return size;
    }
    
    public static int shadowCubeMap() {
        return shadowCubeMap;
    }
    
    public static int fbo() {
        return fbo;
    }
    
    public static void init() {
        
    }
    
    private ShadowCubeMapFBO() {
        
    }
}
