attribute vec4 aPosition;
attribute vec2 aTexCoord;
uniform mat4 aMvpMatrix;
varying vec2 vTexCoord;

void main() {
    gl_Position = aMvpMatrix * aPosition;
    vTexCoord = aTexCoord;
}
