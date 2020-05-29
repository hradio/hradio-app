package lmu.hradio.hradioshowcase.database;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lmu.hradio.hradioshowcase.model.view.ImageData;
import lmu.hradio.hradioshowcase.model.view.RadioServiceViewModel;

/**
 * Database class to keep favorite radio station and downloaded images in local storage
 */
@androidx.room.Database(entities = {Database.RadioServiceEntity.class}, version = 2, exportSchema = false)
public abstract class Database extends RoomDatabase {

    private static final String TAG = Database.class.getSimpleName();

    /**
     * cache to avoid expensive database operations
     */
    private Map<String, RadioServiceEntity> favorites;

    /**
     * Database singleton to avoid multiple expensive connection instances
     */
    private static Database instance;

    /**
     * Change listener collection
     */
    private List<OnFavoritesChangeListener> favoritesListener = new ArrayList<>();

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    Database() {

    }

    /**
     * call init before first use to setup singleton instance
     *
     * @param context - context used for init
     */
    public static void init(Context context) {
        instance = Room.databaseBuilder(context.getApplicationContext(), Database.class, "app-db").addMigrations(MIGRATION_1_2).build();
        new DataBaseAccessTask<Boolean, Integer, Boolean>((b) -> {
            instance.favorites = new HashMap<>();
            List<RadioServiceEntity> services = instance.favoritesDAO().getAllFavorites();
            for (RadioServiceEntity entity : services) {
                instance.favorites.put(entity.serviceLabel, entity);
            }
            return b;
        }, (b) -> {
        }).execute(true);
    }

    /**
     * get singleton instance throws Runtime exception if used before initialization
     *
     * @return instance
     */
    public static Database getInstance() {
        if (instance == null) {
            throw new RuntimeException("Database not initialized");
        }
        return instance;
    }

    /**
     * Favorites table dao - implemented by room's abstraction
     *
     * @return get favorites table dao
     */
    abstract FavoritesDAO favoritesDAO();

    /**
     * Read all favorites from database
     *
     * @param callback - favorites read callback
     */
    public void readFavorites(DatabaseFunctionCallback<List<RadioServiceViewModel>> callback, Context context) {
        List<RadioServiceViewModel> result = new ArrayList<>();
        for (RadioServiceEntity entity : favorites.values()) {
            result.add(toViewModel(entity, context));
        }
        callback.onResult(result);
    }

    /**
     * check if favorites contains a given radio service
     *
     * @param service  - the radio service
     * @param callback - the boolean callback
     */
    public void contains(RadioServiceViewModel service, DatabaseFunctionCallback<Boolean> callback) {
        if (service == null) callback.onResult(false);
        else callback.onResult(favorites.containsKey(service.getServiceLabel()));
    }

    /**
     * Add various services to favorites
     *
     * @param service - services
     */
    private void addToFavorites(RadioServiceViewModel service, Context context) {
        RadioServiceEntity entity = serviceToEntityAndSaveImage(service, context);
        favorites.put(entity.serviceLabel, entity);
        new DataBaseAccessTask<RadioServiceEntity, Integer, Void>(s -> {
            favoritesDAO().insertAll(s);
            return null;
        }, null).executeOnExecutor(executorService,entity);
        for (OnFavoritesChangeListener listener : favoritesListener) {
            listener.onAdded(service);
        }
    }

    /**
     * Delete a given service from database
     *
     * @param service - the radio service
     */
    private void deleteFromFavorites(RadioServiceViewModel service, Context context) {
        favorites.remove(service.getServiceLabel());
        ImageStorageManager.deleteImage(context, service.getServiceLabel());
        new DataBaseAccessTask<RadioServiceViewModel, Integer, Void>(s -> {
            favoritesDAO().delete(serviceToEntity(s));
            return null;
        }, null).executeOnExecutor(executorService, service);
        for (OnFavoritesChangeListener listener : favoritesListener) {
            listener.onRemoved(service);
        }
    }

    /**
     * Toggle service from database:
     * Delete if service is favorite or add if service is no favorite
     *
     * @param service  - the radio sevice
     * @param callback - the boolean callback
     */
    public void toggleFavorite(RadioServiceViewModel service, DatabaseFunctionCallback<Boolean> callback, Context context) {
        contains(service, b -> {
            if (b) {
                deleteFromFavorites(service, context);
                callback.onResult(false);
            } else {
                addToFavorites(service, context);
                callback.onResult(true);
            }
        });
    }

    /**
     * Helper method to transform view model service class to database entity
     *
     * @param serviceView - the radio service
     * @return database entity representation
     */
    private RadioServiceEntity serviceToEntity(RadioServiceViewModel serviceView) {
        RadioServiceEntity res = new RadioServiceEntity();
        res.serviceLabel = serviceView.getServiceLabel();
        res.cover = serviceView.getServiceLabel().trim().replaceAll(" ", "-");
        res.ensembleECC = serviceView.getEnsembleECC();
        res.serviceId = serviceView.getServiceID();
        return res;
    }

    /**
     * Helper method to transform view model service class to database entity and save radio services image to image database
     *
     * @param s - the radio service
     * @return database entity representation
     */
    private RadioServiceEntity serviceToEntityAndSaveImage(RadioServiceViewModel s, Context context) {
        if (s.getLogo() != null && s.getLogo().getImageData() != null)
            ImageStorageManager.saveImage(context, s.getLogo().getImageData(), s.getServiceLabel().trim().replaceAll(" ", "-"));

        return serviceToEntity(s);
    }

    /**
     * Helper method to transform database entity service class to view model
     *
     * @param entity - the radio service
     * @return view model representation
     */
    private RadioServiceViewModel toViewModel(RadioServiceEntity entity, Context context) {
        int serviceID = entity.serviceId != null ? entity.serviceId : 0;
        int ensembleECC = entity.ensembleECC != null ? entity.ensembleECC : 0;
        RadioServiceViewModel res = new RadioServiceViewModel(entity.serviceLabel, serviceID, ensembleECC, context);
        ImageStorageManager.loadImage(context, entity.cover, img ->{
            ImageData data = new ImageData(img, 0, 0);
            res.setImage(data);
        });

        return res;
    }

    public void registerFavoritesChangeListener(OnFavoritesChangeListener listener) {
        this.favoritesListener.add(listener);
    }

    public void unregisterFavoritesChangeListener(OnFavoritesChangeListener listener) {
        this.favoritesListener.remove(listener);
    }

    public void updateImage(RadioServiceViewModel radioServiceViewModel,Context context) {
        if (radioServiceViewModel.getLogo() != null && radioServiceViewModel.getLogo().getImageData() != null)
            ImageStorageManager.saveImage(context, radioServiceViewModel.getLogo().getImageData(), radioServiceViewModel.getServiceLabel().trim().replaceAll(" ", "-"));
    }

    /**
     * Database access object for favorites table
     */
    @Dao
    interface FavoritesDAO {

        /**
         * Read all favorites
         *
         * @return stored favorites
         */
        @Query("SELECT * FROM favorites ORDER BY service_label ASC")
        List<RadioServiceEntity> getAllFavorites();

        /**
         * Find favorite by it's service label
         *
         * @param serviceLabel - the label
         * @return corresponding radio service
         */
        @Query("SELECT * FROM favorites WHERE service_label LIKE :serviceLabel LIMIT 1")
        RadioServiceEntity find(String serviceLabel);

        /**
         * Insert various radio services
         *
         * @param entities - the radio services
         */
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insertAll(RadioServiceEntity... entities);

        /**
         * Delete given radio service
         *
         * @param entity - the radio service
         */
        @Delete
        void delete(RadioServiceEntity entity);


    }

    @Entity(tableName = "favorites", primaryKeys = {"service_label"})
    static class RadioServiceEntity {
        @NonNull
        @ColumnInfo(name = "service_label")
        String serviceLabel;

        @Nullable
        @ColumnInfo(name = "service_cover")
        String cover;

        @Nullable
        @ColumnInfo(name = "service_id")
        Integer serviceId;

        @Nullable
        @ColumnInfo(name = "ensemble_ecc")
        Integer ensembleECC;

    }

    /**
     * Database access task class to perform asynchronous access tasks
     *
     * @param <T> - The input argument type
     * @param <V> - The progress update argument type
     * @param <K> - The return type
     */
    static class DataBaseAccessTask<T, V, K> extends AsyncTask<T, V, K> {

        /**
         * Database access function to be performed
         */
        private DatabaseFunction<T, K> function;
        /**
         * Callback on ui thread performed in on post execute
         */
        @Nullable
        private DatabaseFunctionCallback<K> callback;

        /**
         * Creates new async DataBaseAccessTask
         *
         * @param function - the function to be performed
         * @param callback - callback on ui thread performed in on post execute
         */
        DataBaseAccessTask(@NonNull DatabaseFunction<T, K> function, @Nullable DatabaseFunctionCallback<K> callback) {
            this.function = function;
            this.callback = callback;
        }

        @Override
        protected K doInBackground(T[] ts) {
            K res = null;
            for (T t : ts) {
                res = function.apply(t);
            }
            return res;
        }

        @Override
        protected void onPostExecute(K k) {
            super.onPostExecute(k);
            if (callback != null)
                callback.onResult(k);
        }
    }

    static final Migration MIGRATION_1_2 = new Migration(1,2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE favorites "
                    + " ADD COLUMN service_id INTEGER");
            database.execSQL("ALTER TABLE favorites "
                    + " ADD COLUMN ensemble_ecc INTEGER");
        }

    };

    @FunctionalInterface
    interface DatabaseFunction<T, K> {
        K apply(T t);
    }

    @FunctionalInterface
    public interface DatabaseFunctionCallback<K> {
        @UiThread
        void onResult(K k);
    }

    public interface OnFavoritesChangeListener {
        @UiThread
        void onAdded(RadioServiceViewModel service);

        @UiThread
        void onRemoved(RadioServiceViewModel service);
    }

}
