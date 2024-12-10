(ns app.common-test
  (:require
   [cognitect.test-runner.api]
   [clojure.test :refer :all]
   [app.model :as model]))

(deftest test-update-model
  (testing "Check update X"
    (reset!
     model/objects
     [{:type :circle
       :x 10
       :y 10
       :color {:r 0 :g 0 :b 0}
       :radius 10
       :speed 1
       :vx 1
       :vy 0}])
    (model/update-state!)
    (is (= (get-in @model/objects [0 :x]) 11))
    (is (= (get-in @model/objects [0 :y]) 10)))

  (testing "Check update X"
    (reset!
     model/objects
     [{:type :circle
       :x 10
       :y 10
       :color {:r 0 :g 0 :b 0}
       :radius 10
       :speed 2
       :vx 0.5
       :vy 0.5}])
    (model/update-state!)
    (is (= (get-in @model/objects [0 :x]) 11.0))
    (is (= (get-in @model/objects [0 :y]) 11.0))))

(comment
  (cognitect.test-runner.api/test {})
  )
