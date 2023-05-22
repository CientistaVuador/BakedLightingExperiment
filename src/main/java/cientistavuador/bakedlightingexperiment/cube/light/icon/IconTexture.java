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

import cientistavuador.bakedlightingexperiment.resources.image.ImageResources;
import cientistavuador.bakedlightingexperiment.resources.image.NativeImage;
import static cientistavuador.bakedlightingexperiment.resources.image.NativeImage.USE_ANISOTROPIC_FILTERING;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class IconTexture {
    
    public static final int SPOT_ICON = glGenTextures();
    public static final int POINT_ICON = glGenTextures();
    public static final int LIGHT_OVERLAY = glGenTextures();
    
    private static void load(NativeImage image, int texture) {
        glActiveTexture(GL_TEXTURE0);
        
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RGBA8,
                image.getWidth(),
                image.getHeight(),
                0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                image.getData()
        );
        
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        
        glGenerateMipmap(GL_TEXTURE_2D);
        
        if (USE_ANISOTROPIC_FILTERING && GL.getCapabilities().GL_EXT_texture_filter_anisotropic) {
            glTexParameterf(
                    GL_TEXTURE_2D,
                    GL_TEXTURE_MAX_ANISOTROPY_EXT,
                    glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT)
            );
        }
        
        glBindTexture(GL_TEXTURE_2D, 0);
    }
    
    static {
        NativeImage spotIcon = ImageResources.load("spotlight.png", 4);
        NativeImage pointIcon = ImageResources.load("pointlight.png", 4);
        NativeImage lightOverlay = ImageResources.load("lightcolor.png", 4);
        load(spotIcon, SPOT_ICON);
        load(pointIcon, POINT_ICON);
        load(lightOverlay, LIGHT_OVERLAY);
        spotIcon.free();
        pointIcon.free();
        lightOverlay.free();
    }
    
    private IconTexture() {
        
    }
}
