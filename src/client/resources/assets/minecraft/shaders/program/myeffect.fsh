#version 150

uniform sampler2D DiffuseSampler;

uniform float Time;
uniform float ClickX;
uniform float ClickY;
uniform float Aspect;

in vec2 texCoord;
out vec4 fragColor;

const float maxRadius = 0.5;

float getOffsetStrength(float t, vec2 dir) {
    dir.y /= Aspect;
    float d = length(dir) - t * maxRadius;
    d *= 1.0 - smoothstep(0.0, 0.07, abs(d));
    d *= smoothstep(0.0, 0.07, t);
    d *= 1.0 - smoothstep(0.5, 1.0, t);
    return d;
}

void main() {
    float t = 0.5 * Time;
    vec2 centre = vec2(ClickX, 1.0 - ClickY);
    vec2 pos = texCoord;
    vec2 dir = centre - pos;
    float tOffset = 0.05 * sin(t * 3.14159);

    float rD = getOffsetStrength(t + tOffset, dir);
    float gD = getOffsetStrength(t, dir);
    float bD = getOffsetStrength(t - tOffset, dir);

    // Normalize direction (with safety check)
    vec2 normalizedDir = vec2(0.0);
    if (length(dir) > 0.0001) {
        normalizedDir = normalize(dir);
    }

    // Sample colors with chromatic aberration
    float r = texture(DiffuseSampler, pos + normalizedDir * rD).r;
    float g = texture(DiffuseSampler, pos + normalizedDir * gD).g;
    float b = texture(DiffuseSampler, pos + normalizedDir * bD).b;

    // Add shading effect for more impact
    float shading = gD * 8.0;

    fragColor = vec4(r, g, b, 1.0);
    fragColor.rgb += shading;
}