(ns app.browser
  (:require 
   [app.model :as model]))

(def refresh-time-paint 10)
(def refresh-time-state 30)

(defn init-canvas!
  []
  (let [canvas (.getElementById js/document "canvas")
        ctx (.getContext canvas "2d")]
    (set! (.-width canvas) (.-innerWidth js/window))
    (set! (.-height canvas) (.-innerHeight js/window))
    (.clearRect ctx 0 0 (.-width canvas) (.-height canvas))))

(defn draw-lines
  [ctx]
  (let [width model/window-width 
        height model/window-height]
    (.beginPath ctx)
    (.moveTo ctx 0 0)
    (.lineTo ctx width height)
    (.moveTo ctx width 0)
    (.lineTo ctx 0 height)
    (.stroke ctx)
    (.rect ctx 0 0 width height)
    (.stroke ctx)))

(defmulti draw-object (fn [_ obj] (:type obj)))

(defmethod draw-object :circle
  [ctx {:keys [x y radius color]}]
  (let [{:keys [r g b]} color]
    (set! (.-fillStyle ctx) (str "rgb(" r "," g "," b ")"))
    (.beginPath ctx)
    (.arc ctx x y radius 0 (* 2 Math/PI))
    (.fill ctx)))

(defmethod draw-object :default
  [_ _]
  nil)

(defn draw-frame!
  []
  (js/requestAnimationFrame
   (fn []
     (let [canvas (.getElementById js/document "canvas")
           ctx    (.getContext canvas "2d")]
       (.clearRect ctx 0 0 (.-width canvas) (.-height canvas))
       (draw-lines ctx)
       (doseq [obj @model/objects] (draw-object ctx obj))))))

(defn start-updater
  []
  (js/setInterval model/update-state! refresh-time-state))

(defn start-renderer
  []
  (js/setInterval draw-frame! refresh-time-paint))

(defn ^:dev/after-load start []
  (draw-frame!))

(defn ^:dev/before-load stop []
  (println "stop"))

(defn init []
  (init-canvas!)
  (start-renderer)
  (start-updater)
  (doseq [_ (range 100)]
    (swap! model/objects conj (model/make-random-object)))
  (start))

(comment
  (init-canvas!)
  (draw-frame!) 
  (swap! model/objects conj (model/make-random-object))

  (model/update-state!)
  (start-renderer)
  (start-updater)
  (js/alert "test"))
  
