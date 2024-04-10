package com.programmersbox.common

import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.mikepenz.hypnoticcanvas.shaderBackground
import com.mikepenz.hypnoticcanvas.shaders.*
import com.programmersbox.common.generated.resources.Res
import com.programmersbox.common.generated.resources.card_back
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import okio.Path.Companion.toPath
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.imageResource

private lateinit var dataStore: DataStore<Preferences>

class Settings(
    producePath: () -> String,
) {
    init {
        if (!::dataStore.isInitialized)
            dataStore = PreferenceDataStoreFactory.createWithPath(produceFile = { producePath().toPath() })
    }

    companion object {
        const val DATA_STORE_FILE_NAME = "solitaire.preferences_pb"

        val DIFFICULTY_KEY = stringPreferencesKey("mode_difficulty")
    }
}

@Composable
fun rememberDrawAmount() = rememberPreference(
    intPreferencesKey("draw_amount"),
    DRAW_AMOUNT
)

enum class Difficulty { Easy, Normal }

@Composable
fun rememberModeDifficulty() = rememberPreference(
    Settings.DIFFICULTY_KEY,
    mapToType = { runCatching { Difficulty.valueOf(it) }.getOrNull() },
    mapToKey = { it.name },
    defaultValue = Difficulty.Normal
)

enum class CardBack(
    val brush: @Composable () -> Brush?,
) {
    None({ null }),

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
    }),

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
    }),

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
    }),

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
    });

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

@Composable
fun rememberCardBack() = rememberPreference(
    key = intPreferencesKey("card_back"),
    mapToKey = { it.ordinal },
    mapToType = { CardBack.entries[it] },
    defaultValue = CardBack.None
)

fun <T> preferenceFlow(
    key: Preferences.Key<T>,
    defaultValue: T,
) = dataStore
    .data
    .mapNotNull { it[key] ?: defaultValue }
    .distinctUntilChanged()

fun <T, R> preferenceFlow(
    key: Preferences.Key<T>,
    mapToType: (T) -> R?,
    defaultValue: R,
) = dataStore
    .data
    .mapNotNull { it[key]?.let(mapToType) ?: defaultValue }
    .distinctUntilChanged()

@Composable
fun <T> rememberPreference(
    key: Preferences.Key<T>,
    defaultValue: T,
): MutableState<T> {
    val coroutineScope = rememberCoroutineScope()
    val state by remember(::dataStore.isInitialized) {
        if (::dataStore.isInitialized) {
            dataStore
                .data
                .mapNotNull { it[key] ?: defaultValue }
                .distinctUntilChanged()
        } else {
            emptyFlow()
        }
    }.collectAsStateWithLifecycle(initial = defaultValue)

    return remember(state) {
        object : MutableState<T> {
            override var value: T
                get() = state
                set(value) {
                    coroutineScope.launch {
                        dataStore.edit { it[key] = value }
                    }
                }

            override fun component1() = value
            override fun component2(): (T) -> Unit = { value = it }
        }
    }
}

@Composable
fun <T, R> rememberPreference(
    key: Preferences.Key<T>,
    mapToType: (T) -> R?,
    mapToKey: (R) -> T,
    defaultValue: R,
): MutableState<R> {
    val coroutineScope = rememberCoroutineScope()
    val state by remember(::dataStore.isInitialized) {
        if (::dataStore.isInitialized) {
            dataStore
                .data
                .mapNotNull { it[key]?.let(mapToType) ?: defaultValue }
                .distinctUntilChanged()
        } else {
            emptyFlow()
        }
    }.collectAsStateWithLifecycle(initial = defaultValue)

    return remember(state) {
        object : MutableState<R> {
            override var value: R
                get() = state
                set(value) {
                    coroutineScope.launch {
                        dataStore.edit { it[key] = value.let(mapToKey) }
                    }
                }

            override fun component1() = value
            override fun component2(): (R) -> Unit = { value = it }
        }
    }
}
