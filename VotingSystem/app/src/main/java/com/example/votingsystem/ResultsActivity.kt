package com.example.votingsystem

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ResultsActivity : AppCompatActivity() {

    private lateinit var adapter: ResultsAdapter
    private val resultsList = mutableListOf<CandidateResult>()
    private lateinit var pieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        if (VotingApp.authToken == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val electionId = intent.getStringExtra("ELECTION_ID") ?: ""
        val electionName = intent.getStringExtra("ELECTION_NAME") ?: "Results"

        findViewById<TextView>(R.id.tvResultsTitle).text = electionName

        val rv = findViewById<RecyclerView>(R.id.rvResults)
        val pb = findViewById<ProgressBar>(R.id.pbResults)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarResults)
        pieChart = findViewById(R.id.pieChartResults)

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        rv.layoutManager = LinearLayoutManager(this)
        adapter = ResultsAdapter(resultsList)
        rv.adapter = adapter

        setupPieChart()
        fetchResults(electionId, pb)
    }

    private fun setupPieChart() {
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)
        pieChart.dragDecelerationFrictionCoef = 0.95f
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.WHITE)
        pieChart.transparentCircleRadius = 61f
        pieChart.holeRadius = 58f
        pieChart.legend.isEnabled = true
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setEntryLabelTextSize(12f)
    }

    private fun updateChartData() {
        val entries = ArrayList<PieEntry>()
        resultsList.forEach {
            entries.add(PieEntry(it.votes.toFloat(), it.candidate))
        }

        val dataSet = PieDataSet(entries, "Election Results")
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f
        dataSet.colors = ColorTemplate.JOYFUL_COLORS.toList()

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChart))
        data.setValueTextSize(11f)
        data.setValueTextColor(Color.BLACK)

        pieChart.data = data
        pieChart.invalidate() // refresh
    }

    private fun fetchResults(electionId: String, pb: ProgressBar) {
        pb.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.getResults(electionId)
                }
                if (response.isSuccessful) {
                    resultsList.clear()
                    resultsList.addAll(response.body() ?: emptyList())
                    adapter.notifyDataSetChanged()
                    updateChartData()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ResultsActivity, "Failed to load results", Toast.LENGTH_SHORT).show()
            } finally {
                pb.visibility = View.GONE
            }
        }
    }

    class ResultsAdapter(private val list: List<CandidateResult>) : RecyclerView.Adapter<ResultsAdapter.ViewHolder>() {
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_result, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.name.text = item.candidate
            holder.votes.text = "${item.votes} votes"

            if (!item.symbolUrl.isNullOrEmpty()) {
                try {
                    if (item.symbolUrl.startsWith("data:image")) {
                        val base64String = item.symbolUrl.substringAfter("base64,")
                        val imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                        val decodedImage = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        if (decodedImage != null) {
                            holder.ivSymbol.setImageBitmap(decodedImage)
                            holder.ivSymbol.clearColorFilter()
                        } else {
                            holder.ivSymbol.setImageResource(android.R.drawable.ic_menu_help)
                            holder.ivSymbol.setColorFilter(android.graphics.Color.parseColor("#2C5364"))
                        }
                    } else {
                        holder.ivSymbol.setImageURI(android.net.Uri.parse(item.symbolUrl))
                        holder.ivSymbol.clearColorFilter()
                    }
                } catch (e: Exception) {
                    holder.ivSymbol.setImageResource(android.R.drawable.ic_menu_help)
                    holder.ivSymbol.setColorFilter(android.graphics.Color.parseColor("#2C5364"))
                }
            } else {
                holder.ivSymbol.setImageResource(android.R.drawable.ic_menu_help)
                holder.ivSymbol.setColorFilter(android.graphics.Color.parseColor("#2C5364"))
            }
            
            val maxVotes = list.sumOf { it.votes }.coerceAtLeast(1)
            holder.progress.max = maxVotes
            holder.progress.progress = item.votes
        }

        override fun getItemCount() = list.size

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val name: TextView = view.findViewById(R.id.tvCandidateNameResult)
            val votes: TextView = view.findViewById(R.id.tvVoteCount)
            val progress: ProgressBar = view.findViewById(R.id.pbVotePercent)
            val ivSymbol: android.widget.ImageView = view.findViewById(R.id.ivCandidateSymbolResult)
        }
    }
}
