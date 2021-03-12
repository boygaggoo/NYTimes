package com.smartdubai.nynews.repos


import com.smartdubai.nynews.network.NetworkHelper
import com.smartdubai.nynews.network.api.NewsListService
import com.smartdubai.nynews.network.response.Resource
import com.smartdubai.nynews.network.response.networkBoundResource
import com.smartdubai.nynews.room.ArticlesDao
import com.smartdubai.nynews.room.ResultResponse
import com.smartdubai.nynews.util.API_KEY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/**
 * Used to get data from API service and directly save them to database in order to allow working offline
 */
class NewsRepository @Inject constructor(private val networkHelper: NetworkHelper,
                                         private val articlesListService: NewsListService,
                                         private val articlesDao: ArticlesDao
) {

    @ExperimentalCoroutinesApi
    fun getResults(): Flow<Resource<ResultResponse>> {
        return networkBoundResource(
            fetchFromLocal = { articlesDao.loadAllArticlesFlow() },
            shouldFetchFromRemote = { networkHelper.isNetworkConnected() },
            fetchFromRemote = { articlesDao.deleteCache()
                articlesListService.fetchArticlesList(API_KEY) },
            saveRemoteData = {
                articlesDao.insertArticlesMedia(it) },
            onFetchFailed = { _, _ -> }
        ).flowOn(Dispatchers.IO)
    }

    /**
     * Get Dao to use outside the repository
     */
    fun getArticlesDao() : ArticlesDao{
        return articlesDao
    }
}