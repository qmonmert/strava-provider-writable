/**
 * Created by Quentin on 13/10/15.
 */

/**
 * Require
 */
var gulp        = require('gulp'),
	plumber     = require('gulp-plumber'),
	urlAdjuster = require('gulp-css-url-adjuster'),
	concatCss   = require('gulp-concat-css'),
	del         = require('del'),
	browserSync = require('browser-sync').create(),
	reload      = browserSync.reload,
    minifyCss   = require('gulp-minify-css'),
    compass     = require('gulp-compass');

/**
 * Task : transform sass/strava.scss in css/strava.css
 */
gulp.task('compass', function() {
	return gulp.src('src/**/*.scss')
			.pipe(compass({
			  css:  'src/main/resources/css',
			  sass: 'src/main/resources/sass'
			}));
});

/**
 * Task : delete bundle.min.css and strava.css
 */
gulp.task('clean:css', function() {
	del([
		'src/main/resources/css/bundle.min.css',
		'src/main/resources/css/strava.css'
	]);
});

/**
 * Task : build bundle.min.css with bootstrap.css and strava.css
 * Before start clean:css and compass tasks.
 */
gulp.task('build:css', ['clean:css', 'compass'], function() {
   return gulp.src(['src/**/*.css'])
	   		.pipe(plumber())
	   		.pipe(urlAdjuster({
		    	prepend: '/modules/strava-provider-writable/img/'
			}))
			.pipe(concatCss("bundle.min.css"))
			.pipe(minifyCss({compatibility: 'ie8'}))
			.pipe(gulp.dest('src/main/resources/css/'))
			.pipe(browserSync.stream());
});

/**
 * Task : watch sass/strava.scss
 * Before start buid:css task.
 */
gulp.task('watch', function() {
	gulp.watch(['src/**/*.scss'], ['build:css']);
	gulp.watch('src/**/*.jsp').on('change', reload);
});

/**
 * Task : browser sync
 */
gulp.task('browser-sync', function() {
	browserSync.init({
        proxy: "http://localhost:8080/",
        startPath: "/cms/render/default/en/sites/strava-site/home.html"
    });
});

/**
 * Default task.
 * Before start browser-sync and watch tasks.
 */
gulp.task('default', ['browser-sync', 'watch']);


