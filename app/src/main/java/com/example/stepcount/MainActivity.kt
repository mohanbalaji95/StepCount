package com.example.stepcount

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stepcount.model.StepCountModel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Scope
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import kotlinx.android.synthetic.main.activity_main.*
import java.text.DateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {
    override fun onConnected(p0: Bundle?) {
        Log.e("HistoryAPI", "onConnected")
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.e("HistoryAPI", "onConnectionSuspended")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.e("HistoryAPI", "onConnectionFailed")
    }

    private lateinit var layoutManager: RecyclerView.LayoutManager
    private var mGoogleApiClient: GoogleApiClient? = null;
    val stepsList: ArrayList<StepCountModel> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addApi(Fitness.HISTORY_API)
            .addScope(Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
            .addConnectionCallbacks(this)
            .enableAutoManage(this, 0, this)
            .build()

        ViewWeekStepCountTask().execute()
    }

    override fun onStart() {
        super.onStart()
        //mApiClient?.connect()
    }

    private fun displayLastWeeksData(): Void? {
        val cal: Calendar = Calendar.getInstance()
        val now = Date()
        cal.setTime(now);
        val endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -2);
        val startTime = cal.getTimeInMillis();

        val dateFormat = DateFormat.getDateInstance();

        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        val dataReadResult =
            Fitness.HistoryApi.readData(mGoogleApiClient, readRequest).await(1, TimeUnit.MINUTES)

        //Used for aggregated data
        if (dataReadResult.getBuckets().size > 0) {
            for (bucket in dataReadResult.getBuckets()) {
                val dataSets = bucket.getDataSets();
                for (dataSet in dataSets) {
                    showDataSet(dataSet);
                }
            }
        }
        //Used for non-aggregated data
        else if (dataReadResult.getDataSets().size > 0) {
            for (dataSet in dataReadResult.getDataSets()) {
                showDataSet(dataSet)
            }
        }
        return null
    }

    private fun showDataSet(dataSet: DataSet): Void? {
        val dateFormat = DateFormat.getDateInstance()
        val timeFormat = DateFormat.getTimeInstance()

        for (dp in dataSet.getDataPoints()) {
            lateinit var stepCount: String
            for (field in dp.getDataType().getFields()) {
                if (field.name.equals("steps")) {
                    stepCount = dp.getValue(field).toString()
                }
            }
            stepsList.add(
                StepCountModel(
                    dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)),
                    dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)),
                    stepCount
                )
            )
        }
        return null
    }


    inner class ViewWeekStepCountTask : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            displayLastWeeksData()
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            layoutManager = LinearLayoutManager(applicationContext)
            stepRecyclerView.layoutManager = layoutManager
            stepRecyclerView.adapter = StepCountAdapter(
                context = MainActivity(),
                stepArrayList = stepsList
            )
        }
    }
}

