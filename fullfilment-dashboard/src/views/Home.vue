<template>
  <div class="hero-banner-wrapper">
    <video muted autoplay loop>
      <source
        data-src="//media.aholddelhaize.com/media/ynyn1t4t/2018_tempo_store_3_1.mp4?t=637642752626870000"
        type="video/mp4"
        src="https:////media.aholddelhaize.com/media/ynyn1t4t/2018_tempo_store_3_1.mp4?t=637642752626870000"
      />
    </video>
    <div class="hero-banner-content">
      <div class="text-h4 font-weight-light mb-n1">Welcome to the</div>

      <h1 class="text-h2 font-weight-bold">Pickup Order Task Dashboard</h1>
    </div>
  </div>

  <HelloWorld />
</template>

<script setup>
import HelloWorld from "@/components/HelloWorld.vue";
</script>

<style scoped>
video {
  display: block;
  object-fit: cover;
  width: 100%;
  height: 200px;
  top: 0;
  right: 0;
  overflow-y: clip;
  overflow-x: clip;
}

.hero-banner-wrapper {
  position: relative;
}

.hero-banner-content {
  color: white;
  position: absolute;
  max-width: 105rem;
  padding-left: 20rem;
  padding-right: 20rem;
  margin-left: auto;
  margin-right: auto;
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  /* top: 50%; */
}
</style>
