package com.programmersbox.common

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import com.mikepenz.hypnoticcanvas.shaderBackground
import com.mikepenz.hypnoticcanvas.shaders.*
import com.programmersbox.common.generated.resources.Res
import com.programmersbox.common.generated.resources.card_back
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.imageResource

enum class CardBack(
    val brush: @Composable () -> Brush?,
    val isGsl: Boolean = true,
) {
    None({ null }, false),

    Rainbow({
        Brush.sweepGradient(
            listOf(
                Alizarin,
                Sunflower,
                Emerald,
                Color.Red,
                Color.Green,
                Color.Blue,
                Color.Magenta,
                Color.Yellow,
                Color.Cyan
            )
        )
    }, false),

    Linear({
        Brush.linearGradient(
            listOf(
                Alizarin,
                Sunflower,
                Emerald,
                Color.Red,
                Color.Green,
                Color.Blue,
                Color.Magenta,
                Color.Yellow,
                Color.Cyan
            )
        )
    }, false),

    Radial({
        Brush.radialGradient(
            listOf(
                Alizarin,
                Sunflower,
                Emerald,
                Color.Red,
                Color.Green,
                Color.Blue,
                Color.Magenta,
                Color.Yellow,
                Color.Cyan
            )
        )
    }, false),

    BlackCherry({ null }) {
        @Composable
        override fun toModifier(): Modifier = Modifier.shaderBackground(BlackCherryCosmos)
    },

    GoldenLava({ null }) {
        @Composable
        override fun toModifier(): Modifier = Modifier.shaderBackground(GoldenMagma)
    },

    IceReflect({ null }) {
        @Composable
        override fun toModifier(): Modifier = Modifier.shaderBackground(IceReflection)
    },

    Ink({ null }) {
        @Composable
        override fun toModifier(): Modifier = Modifier.shaderBackground(InkFlow)
    },

    Oil({ null }) {
        @Composable
        override fun toModifier(): Modifier = Modifier.shaderBackground(OilFlow)
    },

    PurpleWater({ null }) {
        @Composable
        override fun toModifier(): Modifier = Modifier.shaderBackground(PurpleLiquid)
    },

    RainbowLiquid({ null }) {
        @Composable
        override fun toModifier(): Modifier = Modifier.shaderBackground(RainbowWater)
    },

    StagePlay({ null }) {
        @Composable
        override fun toModifier(): Modifier = Modifier.shaderBackground(Stage)
    },

    Glossy({ null }) {
        @Composable
        override fun toModifier(): Modifier = Modifier.shaderBackground(GlossyGradients)
    },

    GradientColors({ null }) {
        @Composable
        override fun toModifier(): Modifier = Modifier.shaderBackground(
            remember {
                object : ShaderDelegate() {
                    override val sksl: String = """
                        uniform float uTime;
                        uniform vec3 uResolution;
                        
            vec4 main( vec2 fragCoord ) {
                // Normalized pixel coordinates (from 0 to 1)
                vec2 uv = fragCoord / uResolution.xy;

                // Time varying pixel color
                vec3 col = 0.5 + 0.5*cos(uTime+uv.xyx+vec3(0,2,4));

                // Output to screen
                return vec4(col,1.0);
            }
        """.trimIndent()
                }
            }
        )
    },

    MagicCircle({ null }) {
        @Composable
        override fun toModifier(): Modifier = Modifier.shaderBackground(
            remember {
                object : ShaderDelegate() {
                    override val sksl: String = """
                        uniform float uTime;
                        uniform vec3 uResolution;

const float M_PI = 3.1415926535897932384626433832795;
float M_PI05 = (M_PI * 0.5);

vec2 rotate(vec2 v, float c, float s){
	return vec2(v.x*c - v.y*s, v.x*s + v.y*c);
}

vec2 rotate(vec2 v, float r){
	return rotate(v, cos(r), sin(r));
}

float boxLength(vec2 pos) {
	vec2 q = abs(pos);
	return max(q.x, q.y);
}

float capsuleLength(vec2 pos, vec2 dir) {
	vec2 ba = -dir;
	vec2 pa = pos + ba;
	ba *= 2.0;
	return length(pa - ba * clamp(dot(pa, ba) / dot(ba, ba), -1.0, 1.0));
} 

float triangleLength(vec2 p) {
    p.y += 0.32;
	return max(abs(p.x * 1.8) + p.y, 1.0 - p.y * 1.8) * 0.75;
}

vec2 fracOrigin(vec2 v){
	return (fract(v) - 0.5) * 2.0;
}

float Sa(vec2 pos){
 	float a = capsuleLength(pos + vec2(0.0, -0.7), vec2(0.5, 0.0));   
 	float b = capsuleLength(pos + vec2(-0.3, -0.3), vec2(0.3, 1.3));  
    float c = capsuleLength(pos + vec2(0.3, -0.5), vec2(0, 0.5)); 
    return min(min(a, b), c);
}

float Ke(vec2 pos){
 	float a = capsuleLength(pos + vec2(0.0, -0.3), vec2(0.4, 0.0));   
 	float b = capsuleLength(pos + vec2(0.7, -0.3), vec2(0.5, 0.6));  
    float c = capsuleLength(pos + vec2(0.1, 0.7), vec2(0.3, 0.3));  
    return min(min(a, b), c);
}

float To(vec2 pos){
 	float a = capsuleLength(pos + vec2(0.6, 0.0), vec2(0.4, 1.0));   
 	float b = capsuleLength(pos + vec2(0.0, 0.0), vec2(1.0, -0.8) * 0.4);    
    return min(a, b);
}

float Ba(vec2 pos){
 	float a = capsuleLength(pos + vec2(0.8, 0.0), vec2(0.2, 1.0));   
 	float b = capsuleLength(pos + vec2(-0.8, 0.0), vec2(-0.2, 1.0));     
    float c = length(pos + vec2(-1.0, -1.3));
    float d = length(pos + vec2(-1.2, -0.8));
    return min(min(min(a, b), c), d);
}

float Saketoba(vec2 pos, float power){
    return dot(vec4(1.0), power / (0.01 + vec4(Sa(pos), Ke(pos + vec2(-3.0, 0.0)), To(pos + vec2(-6.0, 0.0)), Ba(pos + vec2(-9.0, 0.0)))));
}

float smoothstepLine(float lower, float upper, float value, float width){
    width *= 0.5;
    return smoothstep(lower - width, lower, value) * (1.0 - smoothstep(upper, upper + width, value));
}

float smoothLine(float value, float target, float width){
    return width / abs(value - target);
}

vec2 smoothLine2(float value, float target, float width){
    return vec2(step(0.0, value - target), width / abs(value - target));
}

float circleTriangle(vec2 pos){
    float circle = length(pos * 0.5);
    float triangle = triangleLength(pos * 0.3);    
    return smoothLine(circle, 1.0, 0.025) + smoothLine(triangle, 1.0, 0.025);
}

vec2 circleTriangle2(vec2 pos){
    float circle2 = length(pos * 0.35);
    vec2 ret = smoothLine2(circle2, 1.0, 0.025);
    ret.y += circleTriangle(pos);
    return ret;
}

float atan2(in float y, in float x)
{
    return x == 0.0 ? sign(y) * M_PI05 : atan(y, x);
}

vec2 polar(vec2 uv) {
	float r = length(uv);
	float s = atan2(uv.y, uv.x) / M_PI;
	return vec2(r, s);
}

float SakeobaCircle(vec2 pos){
    vec2 pp = polar(rotate(pos, -uTime) * 0.75);
    vec2 md = mod(rotate(pp * vec2(2.0, 32.0), M_PI05), vec2(16.0, 4.0));
    return Saketoba(md - vec2(3.5, 1.5), 0.05) * smoothstepLine(6.5, 7.5, pp.x, 1.5) * min(min(1.0, md.x), 16.0 - md.x);
}

float SakeobaCircle2(vec2 pos, float scale, float x, float y, float x2, float y2, float lower, float upper, float r){
    vec2 pp = polar(rotate(pos, r) * scale);
    return Saketoba(mod(rotate(pp * vec2(x, y), M_PI05), vec2(x2, y2)) - 1.5, 0.03) * smoothstepLine(lower, upper, pp.x, 0.2);
}

vec4 main( vec2 fragCoord )
{
    vec2 uv = (fragCoord.xy - uResolution.xy * 0.5) / uResolution.yy * 20.0;     
      
    uv *= clamp(uTime * 0.25, 0.0, 1.0) * 1.1;
        
    uv = rotate(uv, uTime * 0.3);
    
    vec2 uv2 = fragCoord.xy / uResolution.xy;
    
    float len = length(uv);
    
    vec2 c2 = circleTriangle2(uv * 1.4 + vec2(0.0, 8.0));
    vec2 c3 = circleTriangle2(uv * 1.4 + rotate(vec2(0.0, 8.0), M_PI * 2.0 * 0.3333));
    vec2 c4 = circleTriangle2(uv * 1.4 + rotate(vec2(0.0, 8.0), M_PI * 2.0 * 0.6666));
   
    float mask = clamp(c2.x * c3.x * c4.x, 0.0, 1.0);  
        
    float color1 = SakeobaCircle(uv) * 4.0
  		
        + (SakeobaCircle2(uv, 0.995, 8.0, 64.0, 12.0, 4.0, 7.5, 8.0, 5.0 + uTime * 0.2)
        + smoothLine(len, 11.2, 0.1)
        + smoothLine(len, 10.8, 0.1)
        + smoothLine(len, 8.2, 0.01)
        + smoothLine(len, 8.0, 0.02)
        + smoothLine(len, 7.5, 0.01)
        + smoothLine(len, 7.3, 0.01)
        
        + SakeobaCircle2(uv, 1.1, 8.0, 64.0, 12.0, 4.0, 7.5, 7.9, 5.0 + uTime * 0.8) * -1.0
        + 0.0 * SakeobaCircle2(uv, 1.2, 8.0, 64.0, 12.0, 4.0, 7.5, 7.9, 15.0 + uTime * 0.1564)
        + smoothLine(len, 6.7, 0.02)
           
        + SakeobaCircle2(uv, 1.45, 8.0, 64.0, 12.0, 4.0, 7.5, 7.9, 15.0 + uTime * 0.2418654) * -1.0
        + smoothLine(len, 5.0, 0.02)
        + smoothLine(len, 5.5, 0.02)
        
        + SakeobaCircle2(uv, 2.15, 8.0, 64.0, 12.0, 4.0, 7.5, 7.9, 35.0 + uTime * 0.34685) * -1.0
        + SakeobaCircle2(uv, 2.25, 8.0, 64.0, 12.0, 4.0, 7.5, 7.9, 135.0 + uTime * 0.114)
        + SakeobaCircle2(uv, 1.8, 8.0, 64.0, 12.0, 4.0, 7.5, 7.9, 532.0 + uTime * 0.54158)
        + 0.015 / abs(boxLength(rotate(uv, M_PI05 * 0.0 - uTime * 0.5)) - 4.5)
        + 0.005 / abs(boxLength(rotate(uv, M_PI05 * 0.25 - uTime * 0.5)) - 4.5) * -1.0
        + 0.015 / abs(boxLength(rotate(uv, M_PI05 * 0.5 - uTime * 0.5)) - 4.5)
        + 0.005 / abs(boxLength(rotate(uv, M_PI05 * 0.75 - uTime * 0.5)) - 4.5) * -1.0
           
          ) * mask
      
        + circleTriangle(uv)
        ;
    
    float core = pow(0.5, 3.0);
    float core2 = 0.3;
  
    float color2 =   
        + core2 * c2.y
    	+ core2 * c3.y
     	+ core2 * c4.y
        + core / abs(boxLength(uv * vec2(8.0, 0.5) - vec2(0.0, 2.9)) - 1.0)
        + core / abs(boxLength(rotate(uv, M_PI * 2.0 * 0.3333) * vec2(8.0, 0.5) - vec2(0.0, 2.9)) - 1.0)
        + core / abs(boxLength(rotate(uv, M_PI * 2.0 * 0.6666) * vec2(8.0, 0.5) - vec2(0.0, 2.9)) - 1.0)
	;
    
    vec3 col = vec3(
        (color1 + color2 * (1.0)) * 1.5,
    	color1 + color2,
        color1 * 0.2 //change this for purple
    ) * (mask + 1.0);
  
    col += (floor(4.0) - 20.0) * smoothstep(8.0, 8.1, len) * 0.5;
    
    //col *= clamp(2.0, 0.0, 1.0);
    
    return vec4(col, 1.0);
}
        """.trimIndent()
                }
            }
        )
    },

    MagicCircle2({ null }) {
        @Composable
        override fun toModifier(): Modifier = Modifier.shaderBackground(
            remember {
                object : ShaderDelegate() {
                    override val sksl: String = """
                        uniform float uTime;
uniform vec3 uResolution;

const float M_PI = 3.1415926535897932384626433832795;
float M_PI05 = (M_PI * 0.5);


//#define repeat(i, n) for(int i = 0; i < n; i++)

uniform float time;

uniform vec2 resolution;


vec2 rotate(vec2 v, float c, float s){
	return vec2(v.x*c - v.y*s, v.x*s + v.y*c);
}

vec2 rotate(vec2 v, float r){
	return rotate(v, cos(r), sin(r));
}

float boxLength(vec2 pos) {
	vec2 q = abs(pos);
	return max(q.x, q.y);
}

float capsuleLength(vec2 pos, vec2 dir) {
	vec2 ba = -dir;
	vec2 pa = pos + ba;
	ba *= 2.0;
	return length(pa - ba * clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0));
} 

float triangleLength(vec2 p) {
    p.y += 0.32;
	return max(abs(p.x * 1.8) + p.y, 1.0 - p.y * 1.8) * 0.75;
}

vec2 fracOrigin(vec2 v){
	return (fract(v) - 0.5) * 2.0;
}

float Bu(vec2 pos){
 	float a = capsuleLength(pos + vec2(0.0, -0.5), vec2(1.0, 0.0));   
 	float b = capsuleLength(pos + vec2(-0.3, 0.3), vec2(1.0, 1.0) * 0.707);  
    float c = length(pos + vec2(-1.3, -1.3));
    float d = length(pos + vec2(-1.8, -1.3));
    return min(min(min(a, b), c), d);
}

float Chi(vec2 pos){
 	float a = capsuleLength(pos + vec2(0.0, -0.0), vec2(1.0, 0.0));   
 	float b = capsuleLength(pos + vec2(0.0, -1.3), vec2(1.0, 0.8) * 0.4);  
    float c = capsuleLength(pos + vec2(0.0, -0.0), vec2(0.1, 1.0));  
    return min(min(a, b), c);
}

float To(vec2 pos){
 	float a = capsuleLength(pos + vec2(0.5, 0.0), vec2(0.0, 1.0));   
 	float b = capsuleLength(pos + vec2(0.0, 0.0), vec2(1.0, -0.8) * 0.4);    
    return min(a, b);
}

float Ba(vec2 pos){
 	float a = capsuleLength(pos + vec2(0.8, 0.0), vec2(0.3, 1.0));   
 	float b = capsuleLength(pos + vec2(-0.8, 0.0), vec2(-0.3, 1.0));     
    float c = length(pos + vec2(-1.3, -1.3));
    float d = length(pos + vec2(-1.8, -1.3));
    return min(min(min(a, b), c), d);
}

float Butitoba(vec2 pos, float power){
    float ret = 0.0
     + power / Bu(pos)
     + power / Chi(pos + vec2(-3.0, 0.0))
     + power / To(pos + vec2(-6.0, 0.0))
     + power / Ba(pos + vec2(-9.0, 0.0))
        ;
    
    return ret;
}

float smoothstepLine(float lower, float upper, float value, float width){
    width *= 0.5;
    return smoothstep(lower - width, lower, value) * (1.0 - smoothstep(upper, upper + width, value));
}

float smoothLine(float value, float target, float width){
    return width / abs(value - target);
}

vec2 smoothLine2(float value, float target, float width){
    return vec2(step(0.0, value - target), width / abs(value - target));
}

float circleTriangle(vec2 pos){
    float circle = length(pos * 0.5);
    float triangle = triangleLength(pos * 0.3);    
    return smoothLine(circle, 1.0, 0.025) + smoothLine(triangle, 1.0, 0.025);
}

vec2 circleTriangle2(vec2 pos){
    float circle2 = length(pos * 0.35);
    vec2 ret = smoothLine2(circle2, 1.0, 0.025);
    ret.y += circleTriangle(pos);
    return ret;
}

float atan2(in float y, in float x)
{
    return x == 0.0 ? sign(y) * M_PI05 : atan(y, x);
}

vec2 polar(vec2 uv) {
	float r = length(uv);
	float s = atan2(uv.y, uv.x) / M_PI;
	return vec2(r, s);
}

float ButitobaCircle(vec2 pos){
    vec2 pp = polar(rotate(pos, -uTime) * 0.75);
    return Butitoba(mod(rotate(pp * vec2(2.0, 32.0), M_PI05), vec2(16.0, 4.0)) - 1.5, 0.05) * smoothstepLine(6.0, 7.5, pp.x, 1.5);
}

float ButitobaCircle2(vec2 pos, float scale, float x, float y, float x2, float y2, float lower, float upper, float r){
    vec2 pp = polar(rotate(pos, r) * scale);
    return Butitoba(mod(rotate(pp * vec2(x, y), M_PI05), vec2(x2, y2)) - 1.5, 0.03) * smoothstepLine(lower, upper, pp.x, 0.2);
}

vec4 main( vec2 fragCoord )
{
float time =uTime;
  
     vec2 uv2 = (fragCoord.xy - uResolution.xy * 0.5) / uResolution.yy * 20.0;     
     vec2 uv = (fragCoord.xy - uResolution.xy * 0.5) / uResolution.yy * 2.0;     
      
    uv2 *= clamp(uTime * 0.25, 0.0, 1.0);
    
    vec3 col = vec3(0.0, 0.0, 0.0);
        
    uv2 = rotate(uv2, uTime * 0.3);
    
    vec2 c2 = circleTriangle2(uv2 * 1.4 + vec2(0.0, 8.0));
    vec2 c3 = circleTriangle2(uv2 * 1.4 + rotate(vec2(0.0, 8.0), M_PI * 2.0 * 0.3333));
    vec2 c4 = circleTriangle2(uv2 * 1.4 + rotate(vec2(0.0, 8.0), M_PI * 2.0 * 0.6666));
    
    float mask = c2.x * c3.x * c4.x;
    
    float len = length(uv);
	
    col.g += col.b = ButitobaCircle(uv2)
  		
        + (ButitobaCircle2(uv2, 0.995, 8.0, 64.0, 12.0, 4.0, 7.5, 8.0, 5.0 + uTime * 0.2)
        + smoothLine(len, 10.0+0.25*abs(sin(time)), 0.02)
        + smoothLine(len, 7.75+0.25*abs(cos(time)), 0.02)
        + smoothLine(len, 2.75+7.75*abs(mod(time*0.8, 1.0)), 0.02)
           
          ) * mask
      
        + circleTriangle(uv2) 
        + c2.y
    	+ c3.y
     	+ c4.y
        ;
   

        
    vec4 fragColor = vec4(col, 1.0);
    uv.y *= uResolution.y / uResolution.x;
    float mul = uResolution.x / uResolution.y;
    vec3 dir = vec3(uv * mul, 1.);
    float a2 = time * 20. + .5;
    float a1 = 1.0;
    mat2 rot1 = mat2(cos(a1), sin(a1), - sin(a1), cos(a1));
    mat2 rot2 = rot1;
    dir.xz *= rot1;
    dir.xy *= rot2;
    vec3 from = vec3(0., 0., 0.);
    from += vec3(.0025 * time, .03 * time, - 2.);
    from.xz *= rot1;
    from.xy *= rot2;
    float s = .1, fade = .07;
    vec3 v = vec3(0.4);
     for(int r = 0; r < 10; r++) {
	vec3 p = from + s * dir * 1.5;
	p = abs(vec3(0.750) - mod(p, vec3(0.750 * 2.)));
	p.x += float(r * r) * 0.01;
	p.y += float(r) * 0.02;
	float pa, a = pa = 0.;
	for(int i = 0; i < 12; i++) {
	    p = abs(p) / dot(p, p) - 0.340;
	    a += abs(length(p) - pa * 0.2);
	    pa = length(p);
	}
	a *= a * a * 2.;
	v += vec3(s * s , s , s * s) * a * 0.0017 * fade;
	fade *= 0.960;
	s += 0.110;
    }
    v = mix(vec3(length(v)), v, 0.8);
    return vec4(v * 0.01+col, 1.);
}

        """.trimIndent()
                }
            }
        )
    },

    BigBang({ null }) {
        @Composable
        override fun toModifier(): Modifier = Modifier.shaderBackground(
            remember {
                object : ShaderDelegate() {
                    override val sksl: String = """
                        uniform float uTime;
                        uniform vec3 uResolution;
                        
            // https://www.shadertoy.com/view/MdXSzS
// The Big Bang - just a small explosion somewhere in a massive Galaxy of Universes.
// Outside of this there's a massive galaxy of 'Galaxy of Universes'... etc etc. :D

// To fake a perspective it takes advantage of the screen being wider than it is tall.

vec4 main( vec2 fragCoord )
{
	vec2 uv = (fragCoord.xy / uResolution.xy) - .5;
	float t = uTime * .1 + ((.25 + .05 * sin(uTime * .1))/(length(uv.xy) + .07)) * 2.2;
	float si = sin(t);
	float co = cos(t);
	mat2 ma = mat2(co, si, -si, co);

	float v1, v2, v3;
	v1 = v2 = v3 = 0.0;
	
	float s = 0.0;
	for (int i = 0; i < 90; i++)
	{
		vec3 p = s * vec3(uv, 0.0);
		p.xy *= ma;
		p += vec3(.22, .3, s - 1.5 - sin(uTime * .13) * .1);
		for (int i = 0; i < 8; i++)	p = abs(p) / dot(p,p) - 0.659;
		v1 += dot(p,p) * .0015 * (1.8 + sin(length(uv.xy * 13.0) + .5  - uTime * .2));
		v2 += dot(p,p) * .0013 * (1.5 + sin(length(uv.xy * 14.5) + 1.2 - uTime * .3));
		v3 += length(p.xy*10.) * .0003;
		s  += .035;
	}
	
	float len = length(uv);
	v1 *= smoothstep(.7, .0, len);
	v2 *= smoothstep(.5, .0, len);
	v3 *= smoothstep(.9, .0, len);
	
	vec3 col = vec3( v3 * (1.5 + sin(uTime * .2) * .4),
					(v1 + v3) * .3,
					 v2) + smoothstep(0.2, .0, len) * .85 + smoothstep(.0, .6, v3) * .3;

	return vec4(min(pow(abs(col), vec3(1.2)), 1.0), 1.0);
}
        """.trimIndent()
                }
            }
        )
    },

    RainbowCircle({ null }) {
        @Composable
        override fun toModifier(): Modifier = Modifier.shaderBackground(
            remember {
                object : ShaderDelegate() {
                    override val sksl: String = """      
uniform float uTime;
uniform vec3 uResolution;

vec4 main( vec2 fragCoord )
{
//this for horizontal line
	//vec2 uv = fragCoord.xy / uResolution.xy;
//this for circle
vec2 p = (2.0*fragCoord.xy-uResolution.xy)/uResolution.y;

    float a = atan(p.x,p.y);
    float r = length(p);
    vec2 uv = vec2(a/(3.1415926535),r);
//to here for circle

	
	//get the colour
	float xCol = (uv.x - (uTime / 8.0)) * 3.0;
	xCol = mod(xCol, 3.0);
	vec3 horColour = vec3(0.25, 0.25, 0.25);
	
	if (xCol < 1.0) {
		
		horColour.r += 1.0 - xCol;
		horColour.g += xCol;
	}
	else if (xCol < 2.0) {
		
		xCol -= 1.0;
		horColour.g += 1.0 - xCol;
		horColour.b += xCol;
	}
	else {
		
		xCol -= 2.0;
		horColour.b += 1.0 - xCol;
		horColour.r += xCol;
	}
	
	//background lines
	float backValue = 1.0;
	float aspect = uResolution.x / uResolution.y;
	if (mod(uv.y * 100.0, 1.0) > 0.75 || mod(uv.x * 100.0 * aspect, 1.0) > 0.75) {
		
		backValue = 1.15;	
	}
	
	vec3 backLines  = vec3(backValue);
	
	//main beam
	uv = (2.0 * uv) - 1.0;
	float beamWidth = abs(1.0 / (30.0 * uv.y));
	vec3 horBeam = vec3(beamWidth);
	
	return vec4(((backLines * horBeam) * horColour), 1.0);
}
        """.trimIndent()
                }
            }
        )
    },

    GlowingCircles({ null }) {
        @Composable
        override fun toModifier(): Modifier = Modifier.shaderBackground(
            remember {
                object : ShaderDelegate() {
                    override val sksl: String = """
                        uniform float uTime;
                        uniform vec3 uResolution;
                        
            // thanks for those shader
// https://www.shadertoy.com/view/WdK3Dz
// https://www.shadertoy.com/view/3s3GDn

float pix;

float plotCircle(vec2 uv, vec2 pos, float r, float borderWidth) {
    float cir = length(uv-pos)-r;
    //float w = borderWidth * 0.5;
    //return smoothstep(w+2.*pix, w-2.*pix, abs(cir));
    //return step(0., abs(cir)) - step(w, abs(cir));
    return cir;
}

float plotRing(vec2 uv, vec2 pos, float r, float borderWidth) {
    // sdf part, is this part sdf_ring?
    float w = borderWidth * 0.5;
    float cir1 = length(uv-pos) - (r+w);
    float cir2 = length(uv-pos) - (r-w);
    float v = cir1 * cir2;
    //return v;
    
    // just for glow，应该是为了环内部的负数值不要影响glow,发光部分针对的是图案外部像素
    return max(0., v);
}

//https://www.shadertoy.com/view/3s3GDn
float getGlow(float dist, float radius, float intensity){
    return pow(radius/dist, intensity);
}

vec4 main( vec2 fragCoord ) {
    vec2 uv = (fragCoord.xy - uResolution.xy * 0.5)/uResolution.y;
    float ixy = uResolution.x / uResolution.y;
    vec3 c_fin = vec3(0.);
    
    pix = 1. / uResolution.y;
    
    vec3 c_red   = vec3(1.,0.,0.);
    vec3 c_blue  = vec3(0.,1.,0.);
    vec3 c_green = vec3(0.,0.,1.);
    
    float r = 0.2;
    float bw = 30. * pix;
    
    float dFactor = 10.;
    float d1 = plotRing(uv, vec2(0.,r),  r,bw);
    float d2 = plotRing(uv, vec2(-r,0.), r,bw);
    float d3 = plotRing(uv, vec2(r,0.),  r,bw);
    
    float ring1 = smoothstep(0.2 * pix, 0., d1);
    float ring2 = smoothstep(0.2 * pix, 0., d2);
    float ring3 = smoothstep(0.2 * pix, 0., d3);
    
    //c_fin += ring1 + ring2 + ring3;
    c_fin += ring1*vec3(1.,0.25,0.25) 
           + ring2*vec3(0.25,1.,0.25) 
           + ring3*vec3(0.25,0.25,1.);

    float t = (sin(uTime) * 0.5 + 0.5);
    float glowRadius = 2. * pix + 6. * pix * t;
    float intensity = 0.4 + 1. * t;
    
    vec3 glow1 = getGlow(d1, glowRadius, intensity) * c_red;
    vec3 glow2 = getGlow(d2, glowRadius, intensity) * c_blue;
    vec3 glow3 = getGlow(d3, glowRadius, intensity) * c_green;
    
    c_fin += glow1;
    c_fin += glow2;
    c_fin += glow3;
    
    
    
    c_fin = 1. - exp(-c_fin);
    
    return vec4(c_fin,1.0);
}
        """.trimIndent()
                }
            }
        )
    },

    Flame({ null }) {
        @Composable
        override fun toModifier(): Modifier = Modifier.shaderBackground(
            remember {
                object : ShaderDelegate() {
                    override val sksl: String = """
                        uniform float uTime;
                        uniform vec3 uResolution;         
            
// Created by anatole duprat - XT95/2013
// License Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.

float noise(vec3 p) //Thx to Las^Mercury
{
	vec3 i = floor(p);
	vec4 a = dot(i, vec3(1., 57., 21.)) + vec4(0., 57., 21., 78.);
	vec3 f = cos((p-i)*acos(-1.))*(-.5)+.5;
	a = mix(sin(cos(a)*a),sin(cos(1.+a)*(1.+a)), f.x);
	a.xy = mix(a.xz, a.yw, f.y);
	return mix(a.x, a.y, f.z);
}

float sphere(vec3 p, vec4 spr)
{
	return length(spr.xyz-p) - spr.w;
}

float flame(vec3 p)
{
	float d = sphere(p*vec3(1.,.5,1.), vec4(.0,-1.,.0,1.));
	return d + (noise(p+vec3(.0,uTime*2.,.0)) + noise(p*3.)*.5)*.25*(p.y) ;
}

float scene(vec3 p)
{
	return min(100.-length(p) , abs(flame(p)) );
}

vec4 raymarch(vec3 org, vec3 dir)
{
	float d = 0.0, glow = 0.0, eps = 0.02;
	vec3  p = org;
	bool glowed = false;
	
	for(int i=0; i<64; i++)
	{
		d = scene(p) + eps;
		p += d * dir;
		if( d>eps )
		{
			if(flame(p) < .0)
				glowed=true;
			if(glowed)
       			glow = float(i)/64.;
		}
	}
	return vec4(p,glow);
}

vec4 main( vec2 fragCoord ) {
	vec2 v = -1.0 + 2.0 * fragCoord.xy / uResolution.xy;
	v.x *= uResolution.x/uResolution.y;
	
	vec3 org = vec3(0., -2., 4.);
	vec3 dir = normalize(vec3(v.x*1.6, -v.y, -1.5));
	
	vec4 p = raymarch(org, dir);
	float glow = p.w;
	
	vec4 col = mix(vec4(1.,.5,.1,1.), vec4(0.1,.5,1.,1.), p.y*.02+.4);
	
	return mix(vec4(0.), col, pow(glow*2.,4.));
}
        """.trimIndent()
                }
            }
        )
    },

    @OptIn(ExperimentalResourceApi::class)
    Image({
        ShaderBrush(
            ImageShader(
                imageResource(Res.drawable.card_back)
            )
        )
    }, false);

    fun includeGsl() = !isGsl || (isGsl && hasDisplayGsl())

    @Composable
    open fun toModifier(): Modifier? = brush()?.let { Modifier.background(it) }
}

private abstract class ShaderDelegate : Shader {
    override val authorName: String get() = ""
    override val authorUrl: String get() = ""
    override val credit: String get() = ""
    override val license: String get() = ""
    override val licenseUrl: String get() = ""
    override val name: String get() = ""
}