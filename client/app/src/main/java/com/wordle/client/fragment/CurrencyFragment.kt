@file:Suppress("DEPRECATION")

package com.wordle.client.fragment




import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.wordle.client.databinding.FragmentCurrencyBinding
import com.wordle.client.entity.Request
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection


class CurrencyFragment : Fragment() {

    // SET VARIABLE FOR VIDEO VIEW


    // SET VARIABLE BINDING TO FRAGMENT CURRENCY BINDING
    private lateinit var binding: FragmentCurrencyBinding

    // SET VARIABLE VIEW MODEL TO FRAGMENT CURRENCY VIEW-MODEL
    private lateinit var viewModel: CurrencyViewModel

//    private var youTubePlayerView: YouTubePlayerView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCurrencyBinding.inflate(layoutInflater)

        fetchCurrencyData().start()


        return binding.root
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[CurrencyViewModel::class.java]
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
    }

    // FETCH DATA FROM API
    @SuppressLint("SetTextI18n")
    private fun fetchCurrencyData(): Thread
    {
        return Thread {

            // PLACE API URL IN A VARIABLE
            val url = URL("https://open.er-api.com/v6/latest/USD")

            // USE VARIABLE CONNECTION AS HTTPS CONNECTION
            val connection = url.openConnection() as HttpsURLConnection

            // IF CONNECTION RESPONSE IS EQUAL TO 200 (OK)
            if (connection.responseCode == 200) {
                //
                val inputSystem = connection.inputStream

                // THEN LOG (LOGCAT) RESULTS
//                  println(inputSystem.toString())

                val inputStreamReader = InputStreamReader(inputSystem, "UTF-8")

                //
                val request = Gson().fromJson(inputStreamReader, Request::class.java)

                // UPDATE OUR FUNCTION UI REQUEST
                updateUI(request)
                inputStreamReader.close()
                inputSystem.close()
            }
            else {
                // ELSE LOG "FAILED TO CONNECT"
                binding.baseCurrency.text = "Failed to Connect!"
            }
        }
    }


    private fun updateUI(request: Request) {
        //
        requireActivity().runOnUiThread {
            kotlin.run {
                binding.lastUpdated.text = String.format("Updated: " + request.time_last_update_utc)
                binding.nextUpdated.text = String.format("Next Update: " + request.time_next_update_utc)
                binding.nzd.text = String.format("NZD: %.2f", request.rates.NZD)
                binding.aud.text = String.format("AUD: %.2f", request.rates.AUD)
                binding.gbp.text = String.format("GBP: %.2f", request.rates.GBP)
                binding.eur.text = String.format("EUR: %.2f", request.rates.EUR)
                binding.cad.text = String.format("CAD: %.2f", request.rates.CAD)
                binding.mxn.text = String.format("MXN: %.2f", request.rates.MXN)
            }
        }
    }

}

