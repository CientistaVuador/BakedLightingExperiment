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
package cientistavuador.bakedlightingexperiment.cube.light.spot;

import cientistavuador.bakedlightingexperiment.Main;
import cientistavuador.bakedlightingexperiment.cube.Cube;
import cientistavuador.bakedlightingexperiment.cube.light.Light;
import java.util.List;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class SpotLight implements Light {
    private final Vector3f position;
    private final Vector3f direction;
    private final Vector3f diffuseColor;
    private final Vector3f ambientColor;
    private float cutOff = 10f;
    private float outerCutOff = 60.0f;
    private boolean enabled = true;
    
    public SpotLight(Vector3fc position, Vector3f direction, Vector3fc color) {
        this.position = new Vector3f(position);
        this.direction = new Vector3f(direction);
        this.diffuseColor = new Vector3f(color).mul(1.5f);
        this.ambientColor = new Vector3f(color).mul(0.15f);
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getDirection() {
        return direction;
    }

    public float getCutOff() {
        return cutOff;
    }

    public float getOuterCutOff() {
        return outerCutOff;
    }

    public void setCutOff(float cutOff) {
        this.cutOff = cutOff;
    }

    public void setOuterCutOff(float outerCutOff) {
        this.outerCutOff = outerCutOff;
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
        return 0;
    }
    
    @Override
    public void freeShadowMap() {
        
    }
    
    @Override
    public void render(Cube cube, int lightmap) {
        glUseProgram(SpotLightProgram.SHADER_PROGRAM);
        glBindVertexArray(Cube.VAO);
        
        SpotLightProgram.sendUniforms(lightmap, cube.getModel(), cube.getNormalModel(), this);
        glDrawElements(GL_TRIANGLES, Cube.NUMBER_OF_INDICES, GL_UNSIGNED_INT, 0);
        
        Main.NUMBER_OF_DRAWCALLS++;
        Main.NUMBER_OF_VERTICES += Cube.NUMBER_OF_INDICES;
        
        glBindVertexArray(0);
        glUseProgram(0);
    }
    
    @Override
    public void renderShadowMap(List<Cube> cubes) {
        
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
