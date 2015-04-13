(ns shartfinder-ui.routes.home-test
  (:use clojure.test
        shartfinder-ui.routes.home))

;; valid encounter phases:
;; - combatants
;; - initiative
;; - round

(defn my-test-fixture [f]
  (reset! encounter-id nil)
  (f))

(use-fixtures :each my-test-fixture)

(deftest test-sanity
  (is (= 1 1)))

(deftest test-encounter-created?
  (let [jason {:maxHP 4
               :combatantName "faceman"
               :user "jason"}]

    (testing "when nothing has happened yet"
      (is (not (initiative-created?))))

    (testing "when combatants have signed up"
      (reset! combatants #{jason})
      (is (not  (encounter-created?)))
      (is (nil? (get-encounter-phase))))

    (testing "when encounter has been created"
      (reset! encounter-id 69)
      (is (encounter-created?))
      (is (not (nil? (get-encounter-phase))))
      (is (= "initiative" (get-encounter-phase))))))

(deftest test-initiative-created?
  " dont know how to test this"
  )

(deftest test-get-encounter-phase
  (testing "nil when encouter DNE and combatants DNE"
    (is (nil? (get-encounter-phase))))

  (testing "when encounter has been created"
    (with-redefs [encounter-created? (fn [] true)
                  initiative-created? (fn [] false)]
      (let [actual (get-encounter-phase)]
        (is (not (nil? actual)))
        (is (= "initiative" actual)))))

  (testing "when initiative has been completed"
    (with-redefs [encounter-created? (fn [] true)
                  initiative-created? (fn [] true)]
      (let [actual (get-encounter-phase)]
        (is (not (nil? actual)))
        (is (= "round" actual))))))
