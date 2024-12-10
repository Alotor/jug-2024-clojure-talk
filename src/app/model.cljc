(ns app.model)

(def window-width 1024)
(def window-height 768)

(def min-radius 10)
(def max-radius 40)
(def decrease-radius 0.8)
(def kill-radius 5)

(def objects
  (atom [#_{:type :circle :x 100 :y 100 :color "red" :radius 30}]))

(defn rand-int2
  [min max]
  (+ min (rand-int (- max min))))

(defn random-color
  []
  {:r (rand-int 256)
   :g (rand-int 256)
   :b (rand-int 256)})

(defn make-random-object
  []
  (let [radius (rand-int2 min-radius max-radius)
        x (rand-int2 radius (- window-width radius))
        y (rand-int2 radius (- window-height radius))
        color (random-color)
        vx (rand 1)

        s1 (if (= 0 (rand-int 2)) 1 -1)
        s2 (if (= 0 (rand-int 2)) 1 -1)]
    {:type :circle
     :x x
     :y y
     :color color
     :radius radius 
     :speed (+ 2 (rand-int 13))
     :vx (* s1 vx)
     :vy (* s2 (- 1 vx))}))

(defmulti update-object :type)

(defmethod update-object :circle
  [{:keys [speed vx vy radius] :as obj}]
  (let [obj (-> obj
                (update :x + (* speed vx))
                (update :y + (* speed vy)))
        left-bound   (<= (- (:x obj) radius) 0)
        right-bound  (>= (+ (:x obj) radius) window-width)
        top-bound    (<= (- (:y obj) radius) 0)
        bottom-bound (>= (+ (:y obj) radius) window-height)
        obj
        (cond-> obj
          (or left-bound right-bound)
          (update :vx * -1)

          (or top-bound bottom-bound)
          (update :vy * -1)

          (or left-bound right-bound top-bound bottom-bound)
          (update :radius * decrease-radius))]
    (when (>= (:radius obj) kill-radius)
      obj)))

(defmethod update-object :default
  [_]
  nil)

(defn update-state!
  []
  (swap!
   objects
   (fn [objects]
     (->> objects
          (keep update-object)
          (into [])))))

(comment
  (swap! objects update-in [0 :x] + 10)
  (swap! objects update-in [1 :y] + 10)
  (swap! objects assoc-in [1 :color] "cyan")
  (swap! objects conj {:type :circle :x 200 :y 200 :color "red" :radius 30})
  
  (doseq [_ (range 200)]
   (swap! objects conj (make-random-object))) 
  (update-state!)

  (swap! objects assoc-in [0 :x] window-width)
  (swap! objects assoc-in [0 :y] window-height)

  @objects
  (reset! objects [])

  (count @objects)

  ;;(c/foo)
  )
