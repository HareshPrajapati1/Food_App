package com.example.foodapp.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.db.MealDataBase
import com.example.foodapp.pojo.Category
import com.example.foodapp.pojo.CategoryList
import com.example.foodapp.pojo.MealsByCategoryList
import com.example.foodapp.pojo.MealsByCategory

import com.example.foodapp.pojo.Meal
import com.example.foodapp.pojo.MealList
import com.example.foodapp.retrofit.RetrofitInstance
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Query

class HomeViewModel(
    private val mealDataBase: MealDataBase
):ViewModel() {
    private var randomMealLiveData= MutableLiveData<Meal>()
    private var popularItemLiveData=MutableLiveData<List<MealsByCategory>>()
    private var categoriesLiveData=MutableLiveData<List<Category>>()
    private var favoritesMealsLiveData=mealDataBase.mealDao().getAllMealS()
    private var bottomSheetMealLiveData=MutableLiveData<Meal>()
    private val searchMealLiveData=MutableLiveData<List<Meal>>()

 private var saveSateRandomMeal:Meal?=null
    fun getRandomMeal(){

        saveSateRandomMeal?.let {randomMeal->
            randomMealLiveData.postValue(randomMeal)
            return
        }
        RetrofitInstance.api.getRandomMeal().enqueue(object  : Callback<MealList> {
            override fun onResponse(call: Call<MealList>, response: Response<MealList>) {
                if(response.body() != null){
                    val randomMeal: Meal =response.body()!!.meals[0]
                    randomMealLiveData.value=randomMeal
                    saveSateRandomMeal=randomMeal

                }else{
                    return
                }
            }

            override fun onFailure(call: Call<MealList>, t: Throwable) {
                Log.d("HomeFragment",t.message.toString())
            }

        })
    }
    fun observeRandomMealLivedata():LiveData<Meal>{
        return randomMealLiveData
    }

    fun getPopularItems(){
        RetrofitInstance.api.getPopularItems("Seafood").enqueue(object :Callback<MealsByCategoryList>{
            override fun onResponse(call: Call<MealsByCategoryList>, response: Response<MealsByCategoryList>) {
                if(response.body()!=null){
                    popularItemLiveData.value=response.body()!!.meals
                }
            }

            override fun onFailure(call: Call<MealsByCategoryList>, t: Throwable) {
             Log.d("HomeFragment",t.message.toString())
            }
        })
    }
    fun observePopularItemsLiveData():LiveData<List<MealsByCategory>>{
        return popularItemLiveData
    }

    fun getCategories(){
        RetrofitInstance.api.getCategories().enqueue(object : Callback<CategoryList>{
            override fun onResponse(call: Call<CategoryList>, response: Response<CategoryList>) {
                response.body()?.let {categoryList ->
                categoriesLiveData.postValue(categoryList.categories)
                }
            }

            override fun onFailure(call: Call<CategoryList>, t: Throwable) {
                Log.d("HomeViewModel",t.toString())
            }
        })
    }
    fun observeCategoriesLiveData():LiveData<List<Category>>{
        return categoriesLiveData
    }

    fun observeFavoriteMealLiveData():LiveData<List<Meal>>{
        return favoritesMealsLiveData
    }
    fun insertMeal(meal: Meal){
        viewModelScope.launch {
            mealDataBase.mealDao().upsert(meal)
        }
    }

    fun deleteMeal(meal: Meal){
        viewModelScope.launch {
            mealDataBase.mealDao().delete(meal)
        }
    }

    fun getMealById(id:String){
        RetrofitInstance.api.getMealDetails(id).enqueue(object :Callback<MealList>{
            override fun onResponse(call: Call<MealList>, response: Response<MealList>) {
                val meal=response.body()?.meals?.first()
                meal?.let {meal->
                    bottomSheetMealLiveData.postValue(meal)
                }
            }
            override fun onFailure(call: Call<MealList>, t: Throwable) {
                Log.d("HomeViewModel",t.message.toString())
            }
        })
    }
    fun observeBottomSheetMeal():LiveData<Meal> = bottomSheetMealLiveData

    fun searchMeals(searchQuery: String)=RetrofitInstance.api.searchMeal(searchQuery).enqueue(
        object :Callback<MealList>{
            override fun onResponse(call: Call<MealList>, response: Response<MealList>) {
                val mealList=response.body()?.meals
                mealList?.let {
                    searchMealLiveData.postValue(it)
                }
            }
            override fun onFailure(call: Call<MealList>, t: Throwable) {
                Log.d("HomeViewModel",t.message.toString())
            }
        }
    )
    fun observeSearchMealsLiveData():LiveData<List<Meal>> = searchMealLiveData
}