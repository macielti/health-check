(ns health-check.component
  (:require [integrant.core :as ig]
            [clojure.tools.logging :as log]
            [schema.core :as s]))

(s/defn health-check-fn-implemented? :- s/Bool
  [{:keys [health-check-fn]} :- (s/pred map?)]
  (boolean health-check-fn))

(s/defn check-health! :- s/Bool
  [component-key :- s/Keyword
   {:keys [health-check-fn]} :- (s/pred map?)]
  (if health-check-fn
    {component-key (health-check-fn)}
    (do (log/warn :health-check-fn-not-found {:component-key component-key}) false)))

;; to-be-checked is a set of component keys that should be checked for health. If a component key is not in to-be-checked, it will be skipped.
(defmethod ig/init-key ::health-check
  [_ {:keys [components to-be-checked]}]
  (log/info :starting ::health-check)
  (let [results (mapv (fn [[key' component']]
                        (when (to-be-checked key')
                          (check-health! key' component'))) components)]))

(defmethod ig/halt-key! ::health-check
  [_ _health-check]
  (log/info :stopping ::health-check))
