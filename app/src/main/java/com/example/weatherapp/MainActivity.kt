package com.example.weatherapp

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat
import com.airbnb.lottie.LottieAnimationView
import com.example.weatherapp.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//e3a7bd1a88b42b68b9b34ca81c9c0956
class MainActivity : AppCompatActivity() {
    private lateinit var weatherAnimationView: LottieAnimationView
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        weatherAnimationView = findViewById(R.id.anime)
        //binding.searchView

        // Create a SpannableString with the desired hint text and color
        val hintText = "Search"
        val typeface = ResourcesCompat.getFont(this, R.font.nunito_semibold)
        val spannableText = SpannableString(hintText)
        val colorSpan = ForegroundColorSpan(Color.WHITE) // Set your desired color here
        spannableText.setSpan(colorSpan, 0, hintText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Set the custom SpannableString as the query hint for the SearchView
        binding.searchView.queryHint = spannableText

        // Optional: Customize other SearchView properties as needed
        binding.searchView.isIconified = false // Expand SearchView by default

        fetchWeatherData("Mumbai")
        searchCity()

    }
    private fun updateWeatherAnimation(condition: String) {
        val animationResId = when (condition.toLowerCase(Locale.ROOT)) {
            "haze" -> R.raw.haze
            "sunny" -> R.raw.sun
            "rain" -> R.raw.rain
            "wind" -> R.raw.wind
            "clear" -> R.raw.splashscreen
            "snow" -> R.raw.snow
            "thunderstorm" -> R.raw.thunderstrom
            "drizzle" -> R.raw.sunny_rain
            "clouds" -> R.raw.clouds
            else -> R.raw.sun // Default animation for unknown conditions
        }

        // Set the animation resource based on condition
        weatherAnimationView.setAnimation(animationResId)
        weatherAnimationView.playAnimation()
    }

    //change
    private fun searchCity() {
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchWeatherData(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })

    }

    private fun fetchWeatherData(citynm:String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(ApiInterface::class.java)

        val response = retrofit.getWeatherData(citynm, "e3a7bd1a88b42b68b9b34ca81c9c0956", "metric")
        response.enqueue(object : Callback<WeatherApp>{
            @RequiresApi(Build.VERSION_CODES.S)
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody !=null){
                    val temperature = responseBody.main.temp.toString()
                    val humidity = responseBody.main.humidity.toString()
                    val windSpeed = responseBody.wind.speed.toString()
                    val sunRise = responseBody.sys.sunrise.toLong()
                    val sunSet = responseBody.sys.sunset.toLong()
                    val seaLevel = responseBody.main.pressure.toString()
                    val max = responseBody.main.temp_max.toString()
                    val min = responseBody.main.temp_min.toString()
                    val condition = responseBody.weather.firstOrNull()?.main?: "unknown"
                    runOnUiThread {
                        binding.temp.text = "$temperature °C"
                        binding.humidity.text = "$humidity %"
                        binding.wind.text = "$windSpeed m/s"
                        binding.sunrise.text = "${time(sunRise)} "
                        binding.sunset.text = "${time(sunSet)} "
                        binding.sea.text = "$seaLevel hPa"
                        binding.max.text = "Max: $max °C"
                        binding.min.text = "Min: $min °C"
                        binding.weather.text = condition
                        binding.condition.text = condition
                        binding.city.text = "$citynm "
                        binding.day.text = dayName(System.currentTimeMillis())
                        binding.date.text = date()
                        updateWeatherAnimation(condition)
                        //Log.d("TAG", "onResponse : $temperature")
                    }
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                Log.e("MainActivity", "API Call Failed: ${t.message}")
            }

        })
    }
    fun dayName(timestamp: Long): String{
        val df = SimpleDateFormat("EEEE", Locale.getDefault())
        return  df.format((Date()))
    }

    fun date(): String{
        val df = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return  df.format(Date())
    }

    fun time(timestamp: Long): String{
        val df = SimpleDateFormat("HH:mm", Locale.getDefault())
        return  df.format((Date(timestamp*1000)))
    }
}