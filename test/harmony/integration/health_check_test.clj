(ns harmony.integration.health-check-test
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [clj-http.client :as client]
            [harmony.integration.db-test-util :as db-test-util]
            [harmony.config :as config]
            [harmony.system :as system]))

(defonce test-system nil)

(defn- teardown []
  (when-not (nil? test-system)
    (alter-var-root #'test-system component/stop)))

(defn- setup []
  (let [conf (config/config-harmony-api :test)]
    (teardown)
    (db-test-util/reset-test-db (config/migrations-conf conf))
    (alter-var-root
     #'test-system
     (constantly (system/harmony-api conf))))

  (alter-var-root #'test-system component/start))

(use-fixtures :each (fn [f]
                      (setup)
                      (f)
                      (teardown)))

(defn- do-get [endpoint query]
  (client/get (str "http://localhost:8086" endpoint)
              {:query-params query
               :throw-exceptions false}))

(deftest health-check
  (let [{:keys [status body]} (do-get "/_health" {})]

    (is (= 200 status))
    (is (= "HealthCheck" body))))
