#version 330

// normal of the vertex
in vec3 mVertexNormal;
// position of the vertex
in vec3 mVertexPosition;
// texture coordinates
in vec2 mTexCoord;
// color transformation
in vec4 mColor;

struct PointLight
{
    vec3 color;
    vec3 mPosition;
    float intensity;
};

struct DirectionalLight
{
    vec3 color;
    vec3 direction;
    float intensity;
    mat4 lightSpaceMatrix;
};

struct Material
{
    vec4 diffuse;
    vec4 specular;
    float reflectance;
};

const int MAX_POINT_LIGHTS = 16;
const float SHADOW_BIAS = 1.0/(1 << 12);

const float ATT_LIN = 0.1;
const float ATT_EXP = 0.01;

const float LINE_SIZE = 0.0002;
const float LINE_DENSITY = 100;// 500 seems to be the max
const float LINE_ALPHA = 0.3;
const float LINE_COLOR_SENSITIVITY = 2;
const float MINIMUM_LINE_DIST = 0.1;

uniform Material material;
uniform PointLight pointLights[MAX_POINT_LIGHTS];
uniform DirectionalLight directionalLight;

uniform sampler2D texture_sampler;
uniform bool hasTexture;
uniform bool drawHeightLines;

uniform vec3 ambientLight;
uniform float specularPower;
uniform vec3 cameraPosition;

uniform sampler2D dynamicShadowMap;
uniform mat4 viewProjectionMatrix;

// global variables
vec4 diffuse_color;
vec4 specular_color;

// Blinn-Phong lighting
// calculates the diffuse and specular color component caused by one light
vec3 calcBlinnPhong(vec3 light_color, vec3 position, vec3 light_direction, vec3 normal, float light_intensity) {
    // Diffuse component
    float lambertian = max(dot(normal, light_direction), 0.0);
    vec3 diffuse = diffuse_color * lambertian;

    vec3 view_direction = normalize(-position);
    vec3 half_direction = normalize(light_direction + view_direction);
    float specular_angle = max(dot(half_direction, normal), 0.0);
    float specular_factor = pow(specular_angle, specularPower);
    vec3 specular = specular_color * specular_factor

    return (diffuse + specular) * lightColor * lightPower / distance;
}

// Calculate Attenuation
// calculates the falloff of light on a given distance vector
float calcAttenuation(vec3 light_direction) {
    float distance = length(light_direction);
    return (1.0 / (1.0 + ATT_LIN * distance + ATT_EXP * distance * distance));
}

// calculates how much light should remain due to shadow
float calcShadow2D(mat4 lsMatrix, vec3 vPosition, vec3 vNormal, vec3 nLightDir, sampler2D shadowMap) {
    vec4 coord = lsMatrix * vec4(vPosition, 1.0);
    if (coord.z < -1 || coord.z > 1) return 1.0;

    vec3 projCoords = coord.xyz / coord.w;
    projCoords = projCoords * 0.5 + 0.5;

    if (projCoords.x < 0 || projCoords.x > 1) return 1.0;
    if (projCoords.y < 0 || projCoords.y > 1) return 1.0;

    float bias = max(0.01 * (1.0 - dot(vNormal, nLightDir)), SHADOW_BIAS);
    float currentDepth = projCoords.z;
    ivec2 resolution = textureSize(shadowMap, 0);
    ivec2 pixCoord = ivec2(round(projCoords.xy * resolution));

    float light = 1.0;
    {
        float pcfDepth = texelFetch(shadowMap, pixCoord, 0).r;
        float addition = currentDepth - bias < pcfDepth ? 1.0 : 0.0;
        light += addition;
    }{
        float pcfDepth = texelFetch(shadowMap, pixCoord + ivec2(0, -1), 0).r;
        float addition = currentDepth - bias < pcfDepth ? 1.0 : 0.0;
        light += addition;
    }{
        float pcfDepth = texelFetch(shadowMap, pixCoord + ivec2(-1, 0), 0).r;
        float addition = currentDepth - bias < pcfDepth ? 1.0 : 0.0;
        light += addition;
    }{
        float pcfDepth = texelFetch(shadowMap, pixCoord + ivec2(0, 1), 0).r;
        float addition = currentDepth - bias < pcfDepth ? 1.0 : 0.0;
        light += addition;
    }{
        float pcfDepth = texelFetch(shadowMap, pixCoord + ivec2(1, 0), 0).r;
        float addition = currentDepth - bias < pcfDepth ? 1.0 : 0.0;
        light += addition;
    }

    light = min(1.0, light / 5.0);

    return light;
}

// caluclates the color addition caused by a point-light
vec3 calcPointLightComponents(PointLight light) {
    if (light.intensity == 0) return vec3(0, 0, 0);

    vec3 light_direction = light.mPosition - mVertexPosition;
    float att = calcAttenuation(light_direction);

    if (att == 0) {
        return vec3(0, 0, 0);
    } else {
        float attenuatedIntensity = att * light.intensity;
        return calcBlinnPhong(light.color, mVertexPosition, normalize(light_direction), mVertexNormal, attenuatedIntensity);
    }
}

// caluclates the color addition caused by an infinitely far away light, including shadows.
vec3 calcDirectionalLightComponents(DirectionalLight light) {
    if (light.intensity == 0.0){
        return vec3(0, 0, 0);

    } else if (!light.doShadows){
        vec3 nLightDir = normalize(light.direction);
        return calcBlinnPhong(light.color, mVertexPosition, nLightDir, mVertexNormal, light.intensity);

    } else {
        vec3 nLightDir = normalize(light.direction);
        float dynamicShadow = calcShadow2D(light.lightSpaceMatrix, mVertexPosition, mVertexNormal, nLightDir, dynamicShadowMap);
        if (dynamicShadow == 0) return vec3(0, 0, 0);

        vec3 component = calcBlinnPhong(light.color, mVertexPosition, nLightDir, mVertexNormal, light.intensity);
        return component * dynamicShadow;
    }
}

float sigm(float x){
    return x / sqrt(1 + x * x * x);
}

vec4 drawHeightLines(vec4 original_color) {
    float line_visibility = 0;

    float camera_dist = length(cameraPosition - mVertexPosition);
    float target_line_dist = (camera_dist) / LINE_DENSITY;
    float target_level_dist = target_line_dist / MINIMUM_LINE_DIST;

    // calculate distance between markers
    int marker_shift = max(0, int(floor(log2(target_level_dist))));
    int marker_level_dist = 1 << min(marker_shift, 31);

    // calculate distance to nearest marker
    float fragment_level = (mVertexPosition.z / MINIMUM_LINE_DIST);
    float distance_from_level = mod(fragment_level, marker_level_dist);
    float distance_from_height_line = distance_from_level * MINIMUM_LINE_DIST;

    if (abs(distance_from_height_line) < LINE_SIZE * camera_dist){
        // if the next marker_level_dist doesn't activate this, fade out
        if (distance_from_level != mod(fragment_level, marker_level_dist << 1)){
            // foo slightly above 0 means that the camera is almost close enough for solid
            float foo = ((target_level_dist - marker_level_dist) / marker_level_dist);
            line_visibility = max(0, 1 - foo);

        } else {
            line_visibility = 1;
        }
        line_visibility *= LINE_ALPHA;
    }

    float x_component = min(1, max(0, (LINE_COLOR_SENSITIVITY * mVertexNormal.x) + 0.5));
    float y_component = min(1, max(0, (LINE_COLOR_SENSITIVITY * mVertexNormal.y) + 0.5));
    vec4 line_component = vec4(max(y_component, 0), abs(x_component), max(-y_component, 0), 1.0);
    return (1 - line_visibility) * original_color + line_visibility * line_component;
}

void main() {
    if (hasTexture){
        diffuse_color = texture(texture_sampler, mTexCoord);

    } else {
        diffuse_color = mColor * material.diffuse;
    }

    specular_color = material.specular;

    // diffuse and specular color accumulator
    vec3 diffuseSpecular = vec3(0.0, 0.0, 0.0);

    // Calculate directional light
    diffuseSpecular += calcDirectionalLightComponents(directionalLight);

    // Calculate Point Lights
    diffuseSpecular += calcPointLightComponents(pointLights[0]);
    diffuseSpecular += calcPointLightComponents(pointLights[1]);
    diffuseSpecular += calcPointLightComponents(pointLights[2]);
    diffuseSpecular += calcPointLightComponents(pointLights[3]);
    diffuseSpecular += calcPointLightComponents(pointLights[4]);
    diffuseSpecular += calcPointLightComponents(pointLights[5]);
    diffuseSpecular += calcPointLightComponents(pointLights[6]);
    diffuseSpecular += calcPointLightComponents(pointLights[7]);
    diffuseSpecular += calcPointLightComponents(pointLights[8]);
    diffuseSpecular += calcPointLightComponents(pointLights[9]);
    diffuseSpecular += calcPointLightComponents(pointLights[10]);
    diffuseSpecular += calcPointLightComponents(pointLights[11]);
    diffuseSpecular += calcPointLightComponents(pointLights[12]);
    diffuseSpecular += calcPointLightComponents(pointLights[13]);
    diffuseSpecular += calcPointLightComponents(pointLights[14]);
    diffuseSpecular += calcPointLightComponents(pointLights[15]);

    vec4 col = diffuse_color * vec4(ambientLight, 1.0) + vec4(diffuseSpecular, 0.0);
    vec4 col_component = vec4(sigm(col.x), sigm(col.y), sigm(col.z), col.w);

    if (drawHeightLines) {
        col_component = drawHeightLines(col_component)
    }

    vec3 fragColor = diffuse_color * ambientLight + diffuse_specular;
    // apply gamma correction (assume ambientColor, diffuseColor and specColor
    // have been linearized, i.e. have no gamma correction in them)
    vec3 fragColorGammaCorrected = pow(diffuse_specular, vec3(1.0 / SCREEN_GAMMA));
    // use the gamma corrected color in the fragment
    gl_FragColor = vec4(colorGammaCorrected, 1.0);
}