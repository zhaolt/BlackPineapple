precision mediump float;
varying vec2 vTexCoord;
uniform sampler2D sTexture;

void main() {
    lowp float textureColor = texture2D(sTexture, vTexCoord);
    float gray = textureColor.r * 0.299 + textureColor.b * 0.114 + textureColor.g * 0.587;
    gl_FragColor = vec4(gray, gray, gray, textureColor.w);
}
