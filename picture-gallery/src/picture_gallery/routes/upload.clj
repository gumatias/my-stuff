(ns picture-gallery.routes.upload
  (:require [compojure.core :refer [defroutes GET POST]]
            [hiccup.form :refer :all]
            [hiccup.element :refer [image]]
            [hiccup.util :refer [url-encode]]
            [picture-gallery.views.layout :as layout]
            [noir.io :refer [upload-file resource-path]]
            [noir.session :as session]
            [noir.response :as resp]
            [ring.util.response :refer [file-response]]
            [picture-gallery.models.db :as db]
            [clojure.java.io :as io]
            [noir.util.route :refer [restricted]]
            [picture-gallery.util :refer [galleries gallery-path thumb-prefix thumb-uri]]
            [taoensso.timbre :refer [trace debug info warn error fatal]])
  (:import [java.io File FileInputStream FileOutputStream]
           [java.awt.image AffineTransformOp BufferedImage]
           java.awt.RenderingHints
           java.awt.geom.AffineTransform
           javax.imageio.ImageIO))

(def thumb-size 150)

(defn scale [img ratio width height]
  (let [scale (AffineTransform/getScaleInstance 
                (double ratio) (double ratio))
        transform-op (AffineTransformOp.
                       scale AffineTransformOp/TYPE_BILINEAR)]
    (.filter transform-op img (BufferedImage. width height (.getType img)))))

(defn scale-image [file]
  (let [img (ImageIO/read file)
    img-width (.getWidth img)
    img-height (.getHeight img)
    ratio (/ thumb-size img-height)]
    (scale img ratio (int (* img-width ratio)) thumb-size)))

(defn save-thumbnail [{:keys [filename]}]
  (let [path (str (gallery-path) File/separator)]
    (ImageIO/write 
      (scale-image (io/input-stream (str path filename)))
      "jpeg"
      (File. (str path thumb-prefix filename)))))

(defn upload-page [params]
  (layout/render "upload.html" params))

(defn handle-upload [{:keys [filename] :as file}]
  (upload-page 
    (if (empty? filename)
      "please select a file to upload"
      (try 
        ;; save the file and create the thubmnail
        (upload-file (gallery-path) file)
        (save-thumbnail file)
        (db/add-image (session/get :user) filename)
        {:image (thumb-uri (session/get :user) filename)}
        (catch Exception ex
          (error ex "an error has occured while uploading" name)
          (str "error uploading file " (.getMessage ex)))))))

(defn serve-file [user-id file-name]
  (file-response (str galleries File/separator user-id File/separator file-name)))

(defn delete-image [userid name]
  (try
    (db/delete-image userid name)
    (io/delete-file (str (gallery-path) File/separator name))
    (io/delete-file (str (gallery-path) File/separator thumb-prefix name))
    "ok"
    (catch Exception ex 
      (error ex "an error has occured while deleting " name)
      (.getMessage ex))))

(defn delete-images [names]
  (let [userid (session/get :user)]
    (resp/edn
      (for [name names] {:name name :status (delete-image userid name)}))))

(defroutes upload-routes
  (GET "/img/:user-id/:file-name" [user-id file-name] (serve-file user-id file-name))
  (GET "/upload" [info] (restricted (upload-page info)))
  (POST "/upload" [file] (restricted (handle-upload file)))
  (POST "/delete" [names] (restricted (delete-images names))))


