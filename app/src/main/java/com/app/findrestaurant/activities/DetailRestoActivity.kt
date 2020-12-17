@file:Suppress("DEPRECATION")

package com.app.findrestaurant.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.Intent.ACTION_DIAL
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
import android.widget.*
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.app.findrestaurant.*
import com.app.findrestaurant.adapter.HighlightsAdapter
import com.app.findrestaurant.adapter.ReviewAdapter
import com.app.findrestaurant.model.ModelHighlights
import com.app.findrestaurant.model.ModelMain
import com.app.findrestaurant.model.ModelReview
import com.app.findrestaurant.networking.ApiEndpoint.BASEURL
import com.app.findrestaurant.networking.ApiEndpoint.DetailRestaurant
import com.app.findrestaurant.networking.ApiEndpoint.ReviewRestaurant
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.activity_detail_resto.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class DetailRestoActivity : AppCompatActivity() {

    private var mProgressBar: ProgressDialog? = null
    private var highlightsAdapter: HighlightsAdapter? = null
    private var reviewAdapter: ReviewAdapter? = null
    private val modelHighlights: MutableList<ModelHighlights> = ArrayList()
    private val modelReview: MutableList<ModelReview> = ArrayList()

    private var ratingResto = 0.0
    private var idResto: String? = null
    private var imageCover: String? = null
    private var title: String? = null
    private var rating: String? = null
    private var restoName: String? = null
    private var modelMain: ModelMain? = null

    @SuppressLint("Assert", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_resto)

        window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_LAYOUT_STABLE or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        setWindowFlag(this, FLAG_TRANSLUCENT_STATUS, false)
        window.statusBarColor = resources.getColor(R.color.colorPrimary)

        mProgressBar = ProgressDialog(this)
        mProgressBar?.setTitle("Mohon Tunggu")
        mProgressBar?.setCancelable(false)
        mProgressBar?.setMessage("Sedang menampilkan data...")

        toolbar.title = ""
        setSupportActionBar(toolbar)
        assert(supportActionBar != null)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        modelMain = intent.getSerializableExtra(DETAIL_RESTO) as ModelMain
        if (modelMain != null) {
            idResto = modelMain?.idResto
            imageCover = modelMain?.thumbResto
            ratingResto = modelMain!!.aggregateRating
            title = modelMain?.nameResto
            rating = modelMain?.ratingText
            restoName = modelMain?.nameResto

            tvTitle.text = title
            tvRestoName.text = restoName
            tvRating.text = "$ratingResto | $rating"
            tvTitle.isSelected = true
            val newValue = ratingResto.toFloat()

            rating_resto.numStars = 5
            rating_resto.stepSize = 0.5.toFloat()
            rating_resto.rating = newValue

            Glide.with(this)
                .load(imageCover)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgCover)

            //method get Highlight
            showRecyclerViewList()

            //method get Detail
            getDetailResto()

            //method get Review
            getReviewResto()
        }
    }

    private fun showRecyclerViewList() {
        highlightsAdapter = HighlightsAdapter(modelHighlights)
        reviewAdapter = ReviewAdapter(this, modelReview)

        rvHighlights.layoutManager = LinearLayoutManager(this, HORIZONTAL, false)
        rvHighlights.setHasFixedSize(true)
        rvHighlights.adapter = highlightsAdapter

        rvReviewResto.layoutManager = LinearLayoutManager(this)
        rvReviewResto.setHasFixedSize(true)
        rvReviewResto.adapter = reviewAdapter
    }

    private fun getDetailResto() {
        mProgressBar?.show()
        AndroidNetworking.get(BASEURL + DetailRestaurant + idResto)
            .addHeaders("user-key", "API KEY")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                @SuppressLint("SetTextI18n")
                override fun onResponse(response: JSONObject) {
                    try {
                        mProgressBar?.dismiss()
                        val jsonArrayOne = response.getJSONArray("highlights")

                        for (i in 0 until jsonArrayOne.length()) {
                            val dataApi = ModelHighlights()
                            val highlights = jsonArrayOne[i].toString()
                            dataApi.highlights = highlights
                            modelHighlights.add(dataApi)
                        }

                        val jsonObjectData = response.getJSONObject("location")
                        val jsonArrayTwo = response.getJSONArray("establishment")

                        for (x in 0 until jsonArrayTwo.length()) {
                            val establishment = jsonArrayTwo[x].toString()
                            tvEstablishment.text = establishment
                        }

                        val averageCost = response.getString("average_cost_for_two")
                        val priceRange = response.getString("price_range")
                        val currency = response.getString("currency")
                        val timings = response.getString("timings")
                        val localityVerbose = jsonObjectData.getString("locality_verbose")
                        val address = jsonObjectData.getString("address")
                        val telephone = response.getString("phone_numbers")
                        val website = response.getString("url")
                        val latitude = jsonObjectData.getDouble("latitude")
                        val longitude = jsonObjectData.getDouble("longitude")

                        tvLocalityVerbose.text = localityVerbose
                        tvAverageCost.text = "$currency $averageCost / $priceRange orang"
                        tvAddress.text = address
                        tvOpenTime.text = timings

                        llRoute.setOnClickListener {
                            val intent = Intent(ACTION_VIEW,
                                Uri.parse("http://maps.google.com/maps?daddr=$latitude,$longitude"))
                            startActivity(intent)
                        }

                        llTelpon.setOnClickListener {
                            val intent = Intent(ACTION_DIAL, Uri.parse("tel:$telephone"))
                            startActivity(intent)
                        }

                        llWebsite.setOnClickListener {
                            val intent = Intent(ACTION_VIEW, Uri.parse(website))
                            startActivity(intent)
                        }

                        highlightsAdapter?.notifyDataSetChanged()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(this@DetailRestoActivity,
                            "Gagal menampilkan data!", LENGTH_SHORT).show()
                    }
                }

                override fun onError(anError: ANError) {
                    mProgressBar?.dismiss()
                    Toast.makeText(this@DetailRestoActivity,
                        "Tidak ada jaringan internet!", LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun getReviewResto() {
        mProgressBar?.show()
        AndroidNetworking.get(BASEURL + ReviewRestaurant + idResto)
            .addHeaders("user-key", "API KEY")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        mProgressBar?.dismiss()
                        val jsonArray = response.getJSONArray("user_reviews")

                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val dataApi = ModelReview()
                            val jsonObjectDataOne = jsonObject.getJSONObject("review")
                            val jsonObjectDataTwo = jsonObjectDataOne.getJSONObject("user")
                            dataApi.ratingReview = jsonObjectDataOne.getDouble("rating")
                            dataApi.reviewText = jsonObjectDataOne.getString("review_text")
                            dataApi.reviewTime = jsonObjectDataOne.getString("review_time_friendly")
                            dataApi.nameUser = jsonObjectDataTwo.getString("name")
                            dataApi.profileImage = jsonObjectDataTwo.getString("profile_image")
                            modelReview.add(dataApi)
                        }

                        reviewAdapter?.notifyDataSetChanged()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(this@DetailRestoActivity, "Gagal menampilkan data!", LENGTH_SHORT).show()
                    }
                }

                override fun onError(anError: ANError) {
                    mProgressBar?.dismiss()
                    Toast.makeText(this@DetailRestoActivity, "Tidak ada jaringan internet!", LENGTH_SHORT).show()
                }
            })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val DETAIL_RESTO = "detailResto"
        fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
            val window = activity.window
            val layoutParams = window.attributes
            if (on) {
                layoutParams.flags = layoutParams.flags or bits
            } else {
                layoutParams.flags = layoutParams.flags and bits.inv()
            }
            window.attributes = layoutParams
        }
    }
}