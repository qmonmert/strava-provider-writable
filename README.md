### External Data Provider Writable

External data provider to connect [Jahia](https://www.jahia.com/) and [Strava](https://www.strava.com)

1. Installation

    * Generate your strava access tokens :
        * [https://strava.github.io/api/](https://strava.github.io/api/)
        * [http://labs.strava.com/developers/](http://labs.strava.com/developers/)

    * Complete the strava-provider-writable.xml with your access tokens

    * Deploy the module strava-provider-writable on Jahia

    * Create a site named strava-site on Jahia and deploy the module strava-provider-writable on this site

    * Create a page on your site and add a component [jnt:stravaActivities] on this page

2. Utilization

    * Go to the explository explorer

    * Add a new content [jnt:stravaActivity] in the folder /strava-site/contents/strava-activities/

    * Complete the field filename with a path file (GPX or TCX)

    * Go to the page of your site with the component [jnt:stravaActivities] and see that your activity was added
