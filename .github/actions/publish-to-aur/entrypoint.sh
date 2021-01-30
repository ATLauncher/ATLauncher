#!/usr/bin/env bash
# inspired by https://github.com/glitchcrab/action-build-aur-package/blob/main/entrypoint.sh

set -o errexit
set -o pipefail

# base directory
BASEDIR=$(pwd)

# working directory
WORKDIR="$(pwd)/${INPUT_WORKINGDIR}"

main() {
    # all further operations are relative to this directory
    cd "${WORKDIR}"

    # give write access to notroot user
    setfacl -R -m 'u:notroot:rwx' ${BASEDIR}

    # replace pkgver in PKGBUILD with new version
    sed -i "s/pkgver=.*/pkgver=$INPUT_VERSION/g" PKGBUILD

    # prepare git config
    git config --global user.email "${INPUT_AUREMAIL}"
    git config --global user.name "${INPUT_AURUSERNAME}"

    # prepare SSH
    prepare_ssh

    # clone package
    clone_package

    # build the package
    build

    # push package
    push_package
}

prepare_ssh() {
    echo '::group::Preparing SSH'

    if [ ! -d $HOME/.ssh ] ; then
        mkdir -m 0700 $HOME/.ssh
    fi

    # pull down the public key(s) from the AUR servers
    if ! ssh-keyscan -H aur.archlinux.org > $HOME/.ssh/known_hosts ; then
        echo "Couldn't get SSH public key from AUR servers"
        exit 1
    fi

    # write the private SSH key out to disk
    if [ ! -z "${INPUT_AURSSHPRIVATEKEY}" ] ; then
        # write the key out to disk
        echo "${INPUT_AURSSHPRIVATEKEY}" > $HOME/.ssh/ssh_key

        # ensure correct permissions
        chmod 0400 $HOME/.ssh/ssh_key
    fi

    echo '::endgroup::'
}

clone_package() {
    echo '::group::Cloning package from AUR'

    # clone the AUR repo
    export GIT_SSH_COMMAND="ssh -o UserKnownHostsFile=$HOME/.ssh/known_hosts -i $HOME/.ssh/ssh_key"
    if ! git clone "ssh://aur@aur.archlinux.org/${INPUT_PACKAGENAME}.git" /aur_repo; then
        echo "failed to clone AUR repo"
        exit 1
    fi

    # copy over files before building
    cp * /aur_repo
    cp ${BASEDIR}/LICENSE /aur_repo/LICENSE

    chown -R notroot.notroot /aur_repo

    echo '::endgroup::'
}

build() {
    echo '::group::Building package'

    if ! namcap PKGBUILD ; then
        echo "PKGBUILD failed namcap check"
        exit 1
    fi

    # update the package sums
    su notroot -c "updpkgsums"

    # copy over changed PKGBUILD
    cp PKGBUILD /aur_repo

    # install dependencies
    yay -Sy --noconfirm \
        $(pacman --deptest $(source ./PKGBUILD && echo ${depends[@]} ${makedepends[@]}))

    # do the actual building
    su notroot -c "makepkg -f"

    BUILT_PKG_FILE=$(find -name \*pkg.tar.zst)
    if ! namcap ${BUILT_PKG_FILE} ; then
        echo "${BUILT_PKG_FILE} failed namcap check"
        exit 1
    fi

    echo '::endgroup::'
}

push_package() {
    echo '::group::Pushing package to AUR'

    # change to where repo is cloned
    cd /aur_repo

    # update .SRCINFO
    su notroot -c "makepkg --printsrcinfo > .SRCINFO"

    # add files for committing
    if ! git add . ; then
        echo "Couldn't add files for committing"
        exit 1
    fi

    # show current repo state
    git status

    # print out diff
    git --no-pager diff master

    # # commit changes
    git commit -m "${INPUT_AURCOMMITMESSAGE}"

    # # push changes to the repo
    if ! git push ; then
        echo "Couldn't push commit to AUR"
        exit 1
    fi

    echo '::endgroup::'
}

# run
main "$@"
