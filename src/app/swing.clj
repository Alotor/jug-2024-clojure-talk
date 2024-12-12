(ns app.swing
  (:require
   [app.model :as model])
  (:import
   (java.awt Color Graphics2D RenderingHints Dimension)
   (javax.swing JFrame JComponent)
   (java.awt.event WindowAdapter WindowEvent))
  (:gen-class))

(def refresh-time-paint 10)
(def refresh-time-state 30)

(defn bounds
  "Get the bounds for the graphics object"
  [^Graphics2D g]
  (let [bounds (.getClipBounds g)]
    {:x (.getX bounds)
     :y (.getY bounds)
     :width (.getWidth bounds)
     :height (.getHeight bounds)}))

(defn draw-lines
  "Draw two lines crossing from the corners of the canvas"
  [^Graphics2D g]
  (.setColor g Color/BLUE)
  (let [{:keys [width height]} (bounds g)]
    (.clearRect g 0 0 width height)
    (.drawLine g 0 0 model/window-width model/window-height)
    (.drawLine g model/window-width 0 0 model/window-height)))

(defmulti draw-object (fn [_ obj] (:type obj)))

(defmethod draw-object :circle
  [gr {:keys [x y radius color]}]
  (let [{:keys [r g b]} color
        x (- x radius)
        y (- y radius)
        width (* radius 2)]
    (.setColor gr (Color. r g b))
    (.drawOval gr x y width width)
    (.fillOval gr x y width width)))

(defmethod draw-object :default
  [_ _]
  nil)

(defn draw-frame
  [g]
  (draw-lines g)
  (doseq [obj @model/objects] (draw-object g obj)))

(defn create-graphics
  "Create a new graphics object with some custom options for better rendering"
  [g]
  ;; Some values so the canvas is display with antialias
  (doto (.create g)
    (.setRenderingHint RenderingHints/KEY_ALPHA_INTERPOLATION RenderingHints/VALUE_ALPHA_INTERPOLATION_QUALITY)
    (.setRenderingHint RenderingHints/KEY_ALPHA_INTERPOLATION RenderingHints/VALUE_ALPHA_INTERPOLATION_QUALITY)
    (.setRenderingHint RenderingHints/KEY_ANTIALIASING RenderingHints/VALUE_ANTIALIAS_ON)
    (.setRenderingHint RenderingHints/KEY_COLOR_RENDERING RenderingHints/VALUE_COLOR_RENDER_QUALITY)
    (.setRenderingHint RenderingHints/KEY_DITHERING RenderingHints/VALUE_DITHER_ENABLE)
    (.setRenderingHint RenderingHints/KEY_FRACTIONALMETRICS RenderingHints/VALUE_FRACTIONALMETRICS_ON)
    (.setRenderingHint RenderingHints/KEY_INTERPOLATION RenderingHints/VALUE_INTERPOLATION_BILINEAR)
    (.setRenderingHint RenderingHints/KEY_RENDERING RenderingHints/VALUE_RENDER_QUALITY)
    (.setRenderingHint RenderingHints/KEY_STROKE_CONTROL RenderingHints/VALUE_STROKE_PURE)))

(defn make-component
  []
  (proxy
   [JComponent] []
    (paint [g]
      (let [g (create-graphics g)]
        (draw-frame g)))))

(defn make-close-listener
  [callback-fn]
  (proxy
   [WindowAdapter] []
    (windowClosing [^WindowEvent e]
      (callback-fn)
      (let [window (.getWindow e)]
        (.dispose window)))))

(defn make-graphics-updater
  [frame]
  (Thread.
   (fn []
     (try
       (doseq [_ (range)]
         (Thread/sleep refresh-time-paint)
         (.repaint frame))
       (catch Exception e
         (.printStackTrace e))))))

(defn make-state-updater
  []
  (Thread.
   (fn []
     (try
       (doseq [_ (range)]
         (Thread/sleep refresh-time-state)
         (model/update-state!))
       (catch Exception e
         (.printStackTrace e))))))

(def updater (atom nil))

(defn stop-updater
  []
  (when @updater
    (.stop @updater)
    (reset! updater nil)))

(defn create-updater
  []
  (stop-updater)
  (let [update-thread (make-state-updater)]
    (reset! updater update-thread)
    (.start update-thread)
    update-thread))

(defn create-frame
  [& {:keys [exit-on-close?] :or {exit-on-close? false}}]
  (let [frame (new JFrame "Test")

        ;; This thread will repaint the scene
        repaint-thread (make-graphics-updater frame)

        on-close
        (fn []
          (if exit-on-close?
            (System/exit 0)
            (.stop repaint-thread)))]

    (.add (.getContentPane frame) (make-component))
    (.addWindowListener frame (make-close-listener on-close))
    (.setSize frame (Dimension. model/window-width (+ model/window-height 25)))
    (.setResizable frame false)
    (.setVisible frame true)
    (.start repaint-thread)
    nil))

(defn -main
  []
  (doseq [_ (range 200)]
    (swap! model/objects conj (model/make-random-object)))
  (create-frame :exit-on-close? true)
  (create-updater))

(comment
  
  (create-frame)
  (create-updater)
  ;;
  )
  
