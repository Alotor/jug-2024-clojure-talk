(ns app.spec
  (:require [clojure.spec.alpha :as s]))

(s/def :color/r (s/and number? pos? #(<= % 255)))
(s/def :color/g :color/r)
(s/def :color/b :color/r)

(s/def :object/type #{:circle})
(s/def :object/x number?)
(s/def :object/y number?)
(s/def :object/color
  (s/keys
   :req-un
   [:color/r
    :color/g
    :color/b]))
(s/def :object/radius (s/and number? pos?))
(s/def :object/speed (s/and number? pos?))
(s/def :object/vx number?)
(s/def :object/vy number?)

(s/def :model/object
  (s/keys
   :req-un
   [:object/type
    :object/x
    :object/y
    :object/color
    :object/radius]
   :opt-un
   [:object/vx
    :object/vy
    :object/speed]))

(comment
  (s/explain
   :model/object
   {:x 100
    :y 100
    :radius 10
    :speed 10
    :color {:r 255 :g 100 :b 10}}))