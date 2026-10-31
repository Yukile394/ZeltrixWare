#version 150

uniform vec2 uSize;
uniform float Time;
uniform vec4 color1;
uniform vec4 color2;
uniform vec2 uRotation;

out vec4 fragColor;

//License: CC BY 3.0
//Author: Jan Mróz (jaszunio15), adapted for ThunderHack Sky Shader

vec2 hash22(vec2 x)
{
    return fract(sin(x * mat2(43.37862, 24.58974, 32.37621, 53.32761)) * 4534.3897);
}

float hash12(vec2 x)
{
    return fract(sin(dot(x, vec2(43.37861, 34.58761))) * 342.538772);
}

vec2 getCellPoint(vec2 cell)
{
    float time = Time * (hash12(cell + 0.123) - 0.5) * 0.5;
    float c = cos(time), s = sin(time);
    vec2 hash = (hash22(cell) - 0.5) * mat2(c, s, -s, c) + 0.5;
    return hash + cell;
}

float getCellValue(vec2 cell)
{
    return hash12(cell);
}

float makeSmooth(float x)
{
    return mix(x * x * (3.0 - 2.0 * x), sqrt(x), 0.01);
}

float modifiedVoronoiNoise12(vec2 uv)
{
    vec2 rootCell = floor(uv);

    float value = 0.0;

    for (float x = -1.0; x <= 1.0; x++)
    {
        for(float y = -1.0; y <= 1.0; y++)
        {
            vec2 cell = rootCell + vec2(x, y);
            vec2 cellPoint = getCellPoint(cell);
            float cellValue = getCellValue(cell);
            float cellDist = distance(uv, cellPoint);
            value += makeSmooth(clamp(1.0 - cellDist, 0.0, 1.0)) * cellValue;
        }
    }

    return value * 0.5;
}

float layeredNoise12(vec2 x)
{
    float sum = 0.0;
    float maxValue = 0.0;

    for (float i = 1.0; i <= 2.0; i *= 2.0)
    {
        float noise = modifiedVoronoiNoise12(x * i) / i;
        sum += noise;
        maxValue += 1.0 / i;
    }

    return sum / maxValue;
}

void main()
{
    vec2 uv = (gl_FragCoord.xy - 0.5 * uSize.xy) / uSize.y;
    
    // Add camera rotation offset so the sky moves when you look around
    uv.x += uRotation.x * 3.14159265; 
    uv.y += uRotation.y * 3.14159265;
    
    uv.y -= Time * 0.05;
    uv *= 6.0;

    vec4 col = sin(Time * 0.1 + uv.y * 0.2 + vec4(0,2,4,6)) * 0.5 + 0.5;
    // Mix with custom colors for beautiful tinting
    col = mix(col, color1, 0.4);
    
    vec4 col2 = sin(Time * 0.1 + 0.6 + uv.y * 0.2 + vec4(0,2,4,6)) * 0.5 + 0.5;
    col2 = mix(col2, color2, 0.4);

    uv += layeredNoise12(uv);
    float noise = layeredNoise12(uv);
    
    // Stars
    float stars = pow(layeredNoise12(uv * 4.0), 8.0) * 5.0;
    
    fragColor = vec4(smoothstep(-0.14, 1.1, mix(col, col2 * 0.2, 1.0 - noise * 2.0)).rgb + stars, 1.0);
}
