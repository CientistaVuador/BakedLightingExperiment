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

import cientistavuador.bakedlightingexperiment.Main;
import cientistavuador.bakedlightingexperiment.cube.light.Light;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class IconRender {
    
    public static void begin(Matrix4fc projectionView) {
        glUseProgram(IconProgram.SHADER_PROGRAM);
        glBindVertexArray(IconVAO.VAO);
        IconProgram.sendPerFrameUniforms(projectionView);
    }
    
    public static void render(Light light, Matrix4fc viewMatrix) {
        if (!light.isEnabled() || light.getIconType() == null || IconType.NONE.equals(light.getIconType())) {
            return;
        }
        
        Matrix4f model = 
                new Matrix4f()
                .translate(new Vector3f(light.getPosition()))
                .mul(viewMatrix.invert(new Matrix4f()))
                ;
        
        IconProgram.sendPerDrawUniforms(model, light.getIconType().texture(), light.getIconColor());
        glDrawElements(GL_TRIANGLES, IconVAO.NUMBER_OF_INDICES, GL_UNSIGNED_INT, 0);
        
        Main.NUMBER_OF_DRAWCALLS++;
        Main.NUMBER_OF_VERTICES += IconVAO.NUMBER_OF_INDICES;
    }
    
    public static void finish() {
        glBindVertexArray(0);
        glUseProgram(0);
    }
    
    private IconRender() {
        
    }
}
